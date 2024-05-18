package com.ipostu.androidjettycontainer

import android.app.Dialog
import android.app.ProgressDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.text.Html
import android.view.View
import android.widget.Button
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.eclipse.jetty.util.log.Log
import java.util.Date

class MainActivity : PermissionActivity() {

    companion object {

        const val __START_ACTION: String = "org.ijetty.server.app.start"
        const val __STOP_ACTION: String = "org.ijetty.server.app.stop"


        const val __PORT: String = "org.ijetty.server.app.port"
        const val __NIO: String = "org.ijetty.server.app.nio"
        const val __SSL: String = "org.ijetty.server.app.ssl"

        const val __CONSOLE_PWD: String = "org.ijetty.server.app.console"
        const val __PORT_DEFAULT: String = "8080"
        const val __NIO_DEFAULT: Boolean = true
        const val __SSL_DEFAULT: Boolean = false

        const val __CONSOLE_PWD_DEFAULT: String = "admin"

        const val __SETUP_PROGRESS_DIALOG: Int = 0

        // Static initializer block
        init {
            // Ensure parsing is not validating - does not work with android
            System.setProperty("org.eclipse.jetty.xml.XmlParser.Validating", "false")


            // Bridge Jetty logging to Android logging
            System.setProperty(
                "org.eclipse.jetty.util.log.class", "com.ipostu.androidjettycontainer.AndroidLog"
            )
            Log.setLog(AndroidLog())
        }
    }

    private val TAG = "Jetty"

    private val consoleBuffer = StringBuilder()
    private val handler: Handler

    private lateinit var startButton: Button
    private lateinit var stopButton: Button
    private lateinit var configButton: Button
    private lateinit var downloadButton: Button
    private lateinit var progressDialog: ProgressDialog
    private lateinit var consoleScroller: ScrollView
    private lateinit var console: TextView
    private lateinit var info: TextView
    private lateinit var footer: TextView


    private lateinit var broadcastReceiver: BroadcastReceiver
    private var scrollTask: Runnable? = null


    class ConsoleScrollTask(private val consoleScroller: ScrollView) : Runnable {
        override fun run() {
            consoleScroller.fullScroll(View.FOCUS_DOWN)
        }
    }

    init {
        handler = object : Handler() {
            override fun handleMessage(msg: Message) {
                val total = msg.data.getInt("prog")
                progressDialog.setProgress(total)
                if (total >= 100) {
                    dismissDialog(__SETUP_PROGRESS_DIALOG)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        startButton = findViewById(R.id.start)
        stopButton = findViewById(R.id.stop)
        configButton = findViewById(R.id.config)
        downloadButton = findViewById(R.id.download)

        consoleScroller = findViewById(R.id.consoleScroller)
        console = findViewById(R.id.console)
        info = findViewById(R.id.info)
        footer = findViewById(R.id.footer)

        val filter = IntentFilter()
        filter.addAction(__START_ACTION)
        filter.addAction(__STOP_ACTION)
        filter.addCategory("default")

        broadcastReceiver =
            object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    if (__START_ACTION.equals(intent.action, ignoreCase = true)) {
                        startButton.isEnabled = false
                        configButton.isEnabled = false
                        stopButton.isEnabled = true
                        consolePrint("<br/>Started Jetty at %s", Date())
                        val connectors = intent.extras!!.getStringArray("connectors")
                        if (null != connectors) {
                            for (i in connectors.indices) consolePrint(connectors[i])
                        }

                        AppTools.printNetworkInterfaces(consoleBuffer)

                        if (AndroidInfo.isOnEmulator(this@MainActivity)) {
                            consolePrint("Set up port forwarding to see i-jetty outside of the emulator.")
                        } else {
                            consolePrint("")
                        }
                    } else if (__STOP_ACTION.equals(intent.action, ignoreCase = true)) {
                        startButton.isEnabled = true
                        configButton.isEnabled = true
                        stopButton.isEnabled = false
                        consolePrint("<br/> Jetty stopped at %s", Date())
                    }
                }
            }

        registerReceiver(broadcastReceiver, filter)

        startButton.setOnClickListener {
            if (ProgressThread.isUpdateNeeded(this@MainActivity)) AppTools.showQuickToast(
                this@MainActivity,
                R.string.loading
            )
            else {
                //TODO get these values from editable UI elements
                val intent = Intent(
                    this@MainActivity,
                    JettyServerService::class.java
                )
                intent.putExtra(__PORT, __PORT_DEFAULT)
                intent.putExtra(__NIO, __NIO_DEFAULT)
                intent.putExtra(__SSL, __SSL_DEFAULT)
                intent.putExtra(__CONSOLE_PWD, __CONSOLE_PWD_DEFAULT)
                startService(intent)
            }
        }

        stopButton.setOnClickListener {
            stopService(
                Intent(
                    this@MainActivity,
                    JettyServerService::class.java
                )
            )
        }

        val infoBuffer = java.lang.StringBuilder()
        try {
            val pi = packageManager.getPackageInfo(packageName, 0)
            infoBuffer.append(
                AppTools.formatJettyInfoLine(
                    "i-jetty version %s (%s)",
                    pi.versionName,
                    pi.versionCode
                )
            )
        } catch (e: PackageManager.NameNotFoundException) {
            infoBuffer.append(AppTools.formatJettyInfoLine("i-jetty version unknown"))
        }
        infoBuffer.append(
            AppTools.formatJettyInfoLine(
                "On %s using Android version %s",
                AndroidInfo.getDeviceModel(),
                AndroidInfo.getOSVersion()
            )
        )
        info.text = Html.fromHtml(infoBuffer.toString())

        val footerBuffer = java.lang.StringBuilder()
        footerBuffer.append("<b>GitHub:</b>" +
                " <a href=\"https://github.com/IvanPostu\">https://github.com/IvanPostu</a> <br/>")
        footerBuffer.append("<b>Server:</b> http://www.eclipse.org/jetty/ <br/>")
        footer.text = Html.fromHtml(footerBuffer.toString())
    }

    override fun onPermissionDenied() {
    }

    override fun onPermissionGranted() {
    }


    override fun onCreateDialog(id: Int): Dialog? {
        when (id) {
            __SETUP_PROGRESS_DIALOG -> {
                progressDialog = ProgressDialog(this@MainActivity)
                progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
                progressDialog.setMessage("Finishing initial install ...")

                return progressDialog
            }

            else -> return null
        }
    }

    private fun consolePrint(format: String?, vararg args: Any?) {
        val msg = String.format(format!!, *args)
        if (msg.length > 0) {
            consoleBuffer.append(msg).append("<br/>")
            console.setText(Html.fromHtml(consoleBuffer.toString()))
            android.util.Log.i(
                TAG,
                msg
            ) // Only interested in non-empty lines being output to Log
        } else {
            consoleBuffer.append(msg).append("<br/>")
            console.setText(Html.fromHtml(consoleBuffer.toString()))
        }

        if (scrollTask == null) {
            scrollTask = ConsoleScrollTask(consoleScroller)
        }

        consoleScroller.post(scrollTask)
    }
}