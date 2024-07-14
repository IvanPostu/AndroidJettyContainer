package com.ipostu.androidjettycontainer;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import com.ipostu.androidjettycontainer.core.log.WebLoggerCollector;
import com.ipostu.androidjettycontainer.util.AndroidInfo;
import com.ipostu.androidjettycontainer.util.AppTools;

import java.util.Date;

public class MainActivityOld extends PermissionActivity {

    private static final String TAG = "Jetty";

    public static final String __START_ACTION = "com.ipostu.androidjettycontainer.start";
    public static final String __STOP_ACTION = "com.ipostu.androidjettycontainer.stop";

    public static final String __PORT = "com.ipostu.androidjettycontainer.port";
    public static final String __NIO = "com.ipostu.androidjettycontainer.nio";
    public static final String __SSL = "com.ipostu.androidjettycontainer.ssl";

    public static final String __CONSOLE_PWD = "com.ipostu.androidjettycontainer.console";
    public static final String __PORT_DEFAULT = "8080";
    public static final boolean __NIO_DEFAULT = true;
    public static final boolean __SSL_DEFAULT = false;

    public static final String __CONSOLE_PWD_DEFAULT = "admin";

    public static final int __SETUP_PROGRESS_DIALOG = 0;
    public static final int __SETUP_DONE = 2;
    public static final int __SETUP_RUNNING = 1;
    public static final int __SETUP_NOTDONE = 0;

    private Button startButton;
    private Button stopButton;
    private Button configButton;
    private Button downloadButton;

    private TextView info;
    private TextView console;
    private ScrollView consoleScroller;
    private StringBuilder consoleBuffer = new StringBuilder();
    private Runnable scrollTask;
    private ProgressDialog progressDialog;
    private Thread progressThread;
    private Handler handler;
    private BroadcastReceiver bcastReceiver;
    private Handler logsHandler = new Handler();

    static {
        // Ensure parsing is not validating - does not work with android
        System.setProperty("org.eclipse.jetty.xml.XmlParser.Validating", "false");
    }

    class ConsoleScrollTask implements Runnable {
        public void run() {
            consoleScroller.fullScroll(View.FOCUS_DOWN);
        }
    }

    public MainActivityOld() {
        super();
        handler = new Handler() {
            public void handleMessage(Message msg) {
                int total = msg.getData().getInt("prog");
                progressDialog.setProgress(total);
                if (total >= 100) {
                    dismissDialog(__SETUP_PROGRESS_DIALOG);
                }
            }

        };
    }

    @Override
    protected void onPermissionDenied() {
    }

    @Override
    protected void onPermissionGranted() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main_old);

        startButton = findViewById(R.id.start);
        stopButton = findViewById(R.id.stop);
        configButton = findViewById(R.id.config);
        downloadButton = findViewById(R.id.download);

        IntentFilter filter = new IntentFilter();
        filter.addAction(__START_ACTION);
        filter.addAction(__STOP_ACTION);
        filter.addCategory("default");

//        Intent serviceIntent = new Intent(this, MyForegroundService.class);
//        startService(serviceIntent);

        logsHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                String log;
                while ((log = WebLoggerCollector.getInstance().getLogsQueue().poll()) != null) {
                    consolePrint(log);
                }
                logsHandler.postDelayed(this, 3000);
            }
        }, 3000); // Initial delay of 3 seconds

        bcastReceiver =
                new BroadcastReceiver() {

                    public void onReceive(Context context, Intent intent) {
                        if (__START_ACTION.equalsIgnoreCase(intent.getAction())) {
                            startButton.setEnabled(false);
                            configButton.setEnabled(false);
                            stopButton.setEnabled(true);
                            consolePrint("<br/>Started Jetty at %s", new Date());
                            String[] connectors = intent.getExtras().getStringArray("connectors");
                            if (null != connectors) {
                                for (int i = 0; i < connectors.length; i++) {
                                    consolePrint(connectors[i]);
                                }
                            }

                            AppTools.printNetworkInterfaces(consoleBuffer);

                            if (AndroidInfo.isOnEmulator(MainActivityOld.this)) {
                                consolePrint("Set up port forwarding to see i-jetty outside of the emulator.");
                            } else {
                                consolePrint("");
                            }
                        } else if (__STOP_ACTION.equalsIgnoreCase(intent.getAction())) {
                            startButton.setEnabled(true);
                            configButton.setEnabled(true);
                            stopButton.setEnabled(false);
                            consolePrint("<br/> Jetty stopped at %s", new Date());
                        }
                    }

                };

        registerReceiver(bcastReceiver, filter);

        // Watch for button clicks.
        startButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (ProgressThread.isUpdateNeeded(MainActivityOld.this))
                    AppTools.showQuickToast(MainActivityOld.this, R.string.loading);
                else {
                    //TODO get these values from editable UI elements
                    Intent intent = new Intent(MainActivityOld.this, JettyServerService.class);
                    intent.putExtra(__PORT, __PORT_DEFAULT);
                    intent.putExtra(__NIO, __NIO_DEFAULT);
                    intent.putExtra(__SSL, __SSL_DEFAULT);
                    intent.putExtra(__CONSOLE_PWD, __CONSOLE_PWD_DEFAULT);
                    startService(intent);
                }
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                stopService(new Intent(MainActivityOld.this, JettyServerService.class));
            }
        });


        configButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                EditorPreferenceActivity.show(MainActivityOld.this);
            }
        });


        downloadButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                DownloaderActivity.show(MainActivityOld.this);
            }
        });

        info = (TextView) findViewById(R.id.info);
        console = (TextView) findViewById(R.id.console);
        consoleScroller = (ScrollView) findViewById(R.id.consoleScroller);

        StringBuilder infoBuffer = new StringBuilder();
        try {
            PackageInfo pi = getPackageManager().getPackageInfo(getPackageName(), 0);
            infoBuffer.append(AppTools.formatJettyInfoLine("i-jetty version %s (%s)", pi.versionName, pi.versionCode));
        } catch (PackageManager.NameNotFoundException e) {
            infoBuffer.append(AppTools.formatJettyInfoLine("i-jetty version unknown"));
        }
        infoBuffer.append(AppTools.formatJettyInfoLine("On %s using Android version %s", AndroidInfo.getDeviceModel(), AndroidInfo.getOSVersion()));
        info.setText(Html.fromHtml(infoBuffer.toString()));
    }

    private void consolePrint(String format, Object... args) {
        String msg = String.format(format, args);
        if (!msg.isEmpty()) {
            consoleBuffer.append(msg).append("<br/><br/>");
            console.setText(Html.fromHtml(consoleBuffer.toString()));
            Log.i(TAG, msg); // Only interested in non-empty lines being output to Log
        } else {
            consoleBuffer.append(msg).append("<br/>");
            console.setText(Html.fromHtml(consoleBuffer.toString()));
        }

        if (scrollTask == null) {
            scrollTask = new ConsoleScrollTask();
        }

        consoleScroller.post(scrollTask);
    }

    @Override
    protected void onDestroy() {
        if (bcastReceiver != null)
            unregisterReceiver(bcastReceiver);
        handler.removeCallbacksAndMessages(null); // Stop the handler when the activity is destroyed
        logsHandler.removeCallbacksAndMessages(null); // Stop the handler when the activity is destroyed
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        if (!SdCardUnavailableActivity.isExternalStorageAvailable()) {
            SdCardUnavailableActivity.show(this);
        } else {
            //work out if we need to do the installation finish step
            //or not. We do it iff:
            // - there is no previous jetty version on disk
            // - the previous version does not match the current version
            // - we're not already doing the update
            if (ProgressThread.isUpdateNeeded(MainActivityOld.this)) {
                setupJetty();
            }
        }

        if (JettyServerService.isRunning()) {
            startButton.setEnabled(false);
            configButton.setEnabled(false);
            stopButton.setEnabled(true);
        } else {
            startButton.setEnabled(true);
            configButton.setEnabled(true);
            stopButton.setEnabled(false);
        }
        super.onResume();
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case __SETUP_PROGRESS_DIALOG: {
                progressDialog = new ProgressDialog(MainActivityOld.this);
                progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                progressDialog.setMessage("Finishing initial install ...");

                return progressDialog;
            }
            default:
                return null;
        }
    }

    public void setupJetty() {
        showDialog(__SETUP_PROGRESS_DIALOG);
        progressThread = new ProgressThread(MainActivityOld.this, handler);
        progressThread.start();
    }

    public static void show(Context context) {
        final Intent intent = new Intent(context, MainActivityOld.class);
        context.startActivity(intent);
    }

}
