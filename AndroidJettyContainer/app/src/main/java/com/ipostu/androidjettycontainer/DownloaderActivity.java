package com.ipostu.androidjettycontainer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ipostu.androidjettycontainer.util.FileTools;

import org.eclipse.jetty.client.ContentExchange;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.io.Buffer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * IJettyDownloader
 * <p>
 * Download and install new webapp
 */
public class DownloaderActivity extends Activity {
    private static final String TAG = "Jetty";
    public static final int __MSG_DOWNLOAD_SUCCEEDED = 0;
    public static final int __MSG_DOWNLOAD_FAILED = 1;
    public static final int __MSG_PROGRESS = 2;

    private HttpClient client;
    private File tmpDir;
    private ProgressBar _progressBar;

    private File fileInProgress = null;

    private final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case __MSG_DOWNLOAD_SUCCEEDED: {
                    _progressBar.setProgress(100);
                    _progressBar.setVisibility(ProgressBar.INVISIBLE);
                    ((TextView) findViewById(R.id.loading)).setVisibility(TextView.INVISIBLE);
                    ((EditText) findViewById(R.id.download_url)).setText("");
                    ((EditText) findViewById(R.id.context_path)).setText("");
                    fileInProgress = null;
                    AlertDialog.Builder builder = new AlertDialog.Builder(DownloaderActivity.this);
                    builder.setCancelable(true);
                    builder.setMessage(R.string.download_success);
                    builder.setTitle(R.string.success);
                    builder.show();
                    break;
                }
                case __MSG_DOWNLOAD_FAILED: {
                    _progressBar.setProgress(100);
                    _progressBar.setVisibility(ProgressBar.INVISIBLE);
                    ((TextView) findViewById(R.id.loading)).setVisibility(TextView.INVISIBLE);
                    fileInProgress = null;
                    AlertDialog.Builder builder = new AlertDialog.Builder(DownloaderActivity.this);
                    builder.setCancelable(true);
                    builder.setMessage((String) msg.obj);
                    builder.setTitle(R.string.download_fail);
                    builder.show();
                    break;
                }
                case __MSG_PROGRESS: {
                    //onReportProgress(msg.arg1);
                    break;
                }
                default:
                    Log.e(TAG, "Unknown message id " + msg.what);
            }
        }
    };

    public DownloaderActivity() {
        tmpDir = new File(FileTools.getJettyDirPath() + "/" + FileTools.__TMP_DIR);
    }

    /**
     * onCreate
     * <p>
     * Create the download activity.
     *
     * @see Activity#onCreate(Bundle)
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!tmpDir.exists())
            tmpDir.mkdirs();

        setContentView(R.layout.jetty_downloader);
        _progressBar = (ProgressBar) findViewById(R.id.progress);
        final Button startDownloadButton = (Button) findViewById(R.id.start_download);
        startDownloadButton.setOnClickListener(
                new OnClickListener() {
                    public void onClick(View v) {
                        EditText text = (EditText) findViewById(R.id.download_url);
                        String url = text.getText().toString();
                        if (url == null)
                            return;
                        if ("".equals(url.trim()))
                            return;

                        text = (EditText) findViewById(R.id.context_path);
                        String path = text.getText().toString();
                        startDownload(url, path);
                    }
                }
        );
    }

    /**
     * User has moved away from the download activity.
     * Stop the httpclient.
     *
     * @see Activity#onPause()
     */
    protected void onPause() {
        super.onPause();
        try {
            if (client != null) {
                client.stop();
                Log.i(TAG, "Stopped httpclient");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error stopping httpclient ", e);
        } finally {
            client = null;
            if (fileInProgress != null) {
                _progressBar.setVisibility(ProgressBar.INVISIBLE);
                ((TextView) findViewById(R.id.loading)).setVisibility(TextView.INVISIBLE);
                ((EditText) findViewById(R.id.download_url)).setText("");
                ((EditText) findViewById(R.id.context_path)).setText("");

                //don't leave things half done
                if (fileInProgress != null)
                    WebappInstaller.clean(FileTools.getJettyDirPath(), fileInProgress);
                fileInProgress = null;
            }
        }
    }

    /**
     * Download activity is being stopped.
     * Stop the httpclient (note that onPause
     * should always be called first, so the
     * client should be null).
     *
     * @see Activity#onPause()
     */
    protected void onStop() {
        super.onStop();
        try {
            if (client != null) {
                client.stop();
                Log.i(TAG, "Stopped httpclient");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error stopping httpclient ", e);
        } finally {
            client = null;
            fileInProgress = null;
        }
    }

    /**
     * Begin a download of a war file.
     *
     * @param url
     */
    public void startDownload(final String url, final String path) {
        final String war = getWarFileName(url);
        final File warFile = new File(tmpDir, war);
        try {
            if (!warFile.createNewFile()) {
                Log.i(TAG, war + ": File exists");
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setCancelable(true);
                builder.setMessage(R.string.overwrite);
                builder.setTitle(R.string.webapp_exists);

                builder.setPositiveButton(R.string.yes,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface arg0, int arg1) {
                                WebappInstaller.clean(FileTools.getJettyDirPath(), warFile);
                                doDownload(url, warFile, path);
                            }
                        });
                builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                    }
                });
                builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    public void onCancel(DialogInterface arg0) {
                    }
                });
                builder.show();
            } else
                Log.d(TAG, "Download: " + url + ", path: " + path);
                doDownload(url, warFile, path);
        } catch (Exception e) {
            Log.e(TAG, "Error creating file " + war, e);
            return;
        }
    }

    /**
     * Download and install the file as a webapp.
     *
     * @param url
     * @param warFile
     */
    public void doDownload(final String url, final File warFile, final String path) {
        try {
            //lazily create the httpclient
            if (client == null) {
                client = new HttpClient();
                client.setConnectorType(HttpClient.CONNECTOR_SOCKET);
                client.setMaxConnectionsPerAddress(1);
                client.setUseDirectBuffers(false);
            }

            if (!client.isRunning())
                client.start();
        } catch (Exception e) {
            Log.e(TAG, "Error starting client", e);
            mHandler.sendMessage(Message.obtain(mHandler, __MSG_DOWNLOAD_FAILED, "Failed to start client"));
            return;
        }

        //Get the file
        fileInProgress = warFile;
        _progressBar.setVisibility(ProgressBar.VISIBLE);
        _progressBar.setProgress(0);
        _progressBar.setIndeterminate(true);
        ((TextView) findViewById(R.id.loading)).setVisibility(TextView.VISIBLE);

        ContentExchange exchange = new ContentExchange() {
            private OutputStream _outputStream;

            protected void onResponseComplete() throws IOException {
                closeOutputStream();
                if (getResponseStatus() == HttpStatus.OK_200)
                    install(warFile, path);
                else {
                    Log.e(TAG, "Bad status: " + getResponseStatus());
                    mHandler.sendMessage(Message.obtain(mHandler, __MSG_DOWNLOAD_FAILED, "Bad status: " + getResponseStatus()));
                }
            }

            protected void onConnectionFailed(Throwable ex) {
                closeOutputStream();
                mHandler.sendMessage(Message.obtain(mHandler, __MSG_DOWNLOAD_FAILED, "Connection failed"));
                Log.e(TAG, "Connection fail", ex);
                super.onConnectionFailed(ex);
            }

            protected void onException(Throwable ex) {
                closeOutputStream();
                mHandler.sendMessage(Message.obtain(mHandler, __MSG_DOWNLOAD_FAILED, "Exception"));
                Log.e(TAG, "Error on download", ex);
                super.onException(ex);
            }

            protected void onExpire() {
                closeOutputStream();
                mHandler.sendMessage(Message.obtain(mHandler, __MSG_DOWNLOAD_FAILED, "Expired"));
                Log.e(TAG, "Expired: " + url);
                super.onExpire();
            }

            protected void onResponseContent(Buffer content)
                    throws IOException {
                try {
                    OutputStream os = getOutputStream();
                    content.writeTo(os);
                } catch (Exception e) {
                    Log.e(TAG, "Error reading content", e);
                    mHandler.sendMessage(Message.obtain(mHandler, __MSG_DOWNLOAD_FAILED, "Exception"));
                }
            }

            protected OutputStream getOutputStream()
                    throws IOException {
                if (_outputStream == null) {
                    _outputStream = new FileOutputStream(warFile);
                }
                return _outputStream;
            }

            protected void closeOutputStream() {
                try {
                    if (_outputStream != null)
                        _outputStream.close();
                } catch (IOException e) {
                    Log.e(TAG, "Error closing stream", e);
                    mHandler.sendMessage(Message.obtain(mHandler, __MSG_DOWNLOAD_FAILED, "Exception"));
                }
            }
        };

        exchange.setURL(url);
        try {
            Log.i(TAG, "Downloading " + url);
            client.send(exchange);
        } catch (Exception e) {
            Log.e(TAG, "Download failed for " + url);
        }
    }

    public String getWarFileName(String url) {
        if (url == null)
            return null;
        try {
            int dot = url.lastIndexOf("?");
            String war = (dot < 0 ? url : url.substring(0, dot));
            dot = url.lastIndexOf('/');
            war = (dot < 0 ? url : url.substring(dot + 1));
            return war;
        } catch (Exception e) {
            Log.e(TAG, "Bad url " + url);
            return null;
        }
    }

    public void install(File file, String path) {
        try {
            File webappDir = new File(FileTools.getJettyDirPath() + "/" + FileTools.__WEBAPP_DIR);
            String name = file.getName();
            if (name.endsWith(".war") || name.endsWith(".jar"))
                name = name.substring(0, name.length() - 4);

            WebappInstaller.install(file, path, webappDir, name, true);
            mHandler.sendMessage(Message.obtain(mHandler, __MSG_DOWNLOAD_SUCCEEDED));
        } catch (Exception e) {
            Log.e(TAG, "Bad resource", e);
            mHandler.sendMessage(Message.obtain(mHandler, __MSG_DOWNLOAD_FAILED, "Exception"));
        }
    }

    public static void show(Context context) {
        final Intent intent = new Intent(context, DownloaderActivity.class);
        context.startActivity(intent);
    }
}
