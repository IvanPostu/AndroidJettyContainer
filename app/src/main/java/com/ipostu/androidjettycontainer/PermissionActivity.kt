package com.ipostu.androidjettycontainer


import android.Manifest
import android.annotation.TargetApi
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

abstract class PermissionActivity : AppCompatActivity() {
    private var hasPermission = true

    @TargetApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestPermission()

        if (hasPermission) {
            onPermissionGranted()
        } else {
            onPermissionDenied()
        }
    }

    protected abstract fun onPermissionDenied()

    protected abstract fun onPermissionGranted()

    private val neededPermissions: Array<String>
        get() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                return arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
            return arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val permissions = getDeniedPermissions(neededPermissions)
            if (permissions.size != 0) {
                hasPermission = false
                ActivityCompat.requestPermissions(
                    this,
                    permissions,
                    REQUEST_CODE
                )
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    if (!haveAllFilesAccessPermission()) {
                        hasPermission = false
                        requestAllFilesAccessPermission()
                    }
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode != REQUEST_CODE) {
            return
        }

        var grantedPermissions = 0
        for (result in grantResults) {
            if (result == PackageManager.PERMISSION_GRANTED) {
                grantedPermissions++
            }
        }

        if (grantedPermissions == grantResults.size) {
            hasPermission = true
            Log.i(TAG, "User granted permission.")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (!haveAllFilesAccessPermission()) {
                    hasPermission = false
                    requestAllFilesAccessPermission()
                }
            }

            //            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//                if (!haveAllFilesAccessPermission()) {
//                    requestAllFilesAccessPermission();
//                }
//            }
        } else {
            Log.w(TAG, "User denied permission.")
            Toast.makeText(this, R.string.dialog_storage_permission_not_granted, Toast.LENGTH_SHORT)
                .show()
            //            Intent intent = new Intent(this, PermissionActivity.class);
//            this.startActivity(intent);
        }
    }

    // Return to judgment from system permission setting page
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!haveAllFilesAccessPermission()) {
                Toast.makeText(
                    this,
                    R.string.dialog_all_files_access_not_supported,
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                hasPermission = true
            }
        }
    }

    private fun getDeniedPermissions(permissions: Array<String>): Array<String> {
        val ret: MutableList<String> = ArrayList()
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ret.add(permission)
            }
        }
        return ret.toTypedArray<String>()
    }

    @TargetApi(30)
    private fun haveAllFilesAccessPermission(): Boolean {
        return Environment.isExternalStorageManager()
    }

    @TargetApi(30)
    private fun requestAllFilesAccessPermission() {
        var intentFailed = false
        val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
        intent.setData(Uri.parse("package:$packageName"))
        try {
            val componentName = intent.resolveActivity(packageManager)
            if (componentName != null) {
                val className = componentName.className
                if (className != null) {
                    // Launch "Allow all files access?" dialog.
                    //startActivity(intent);
                    startActivityForResult(intent, REQUEST_CODE)
                    return
                }
                intentFailed = true
            } else {
                Log.w(TAG, "Request all files access not supported")
                intentFailed = true
            }
        } catch (e: ActivityNotFoundException) {
            Log.w(TAG, "Request all files access not supported", e)
            intentFailed = true
        }
        if (intentFailed) {
            // Some devices don't support this request.
            Toast.makeText(this, R.string.dialog_all_files_access_not_supported, Toast.LENGTH_LONG)
                .show()
        }
    }


    companion object {
        private const val TAG = "Jetty"
        private const val REQUEST_CODE = 1024
    }
}
