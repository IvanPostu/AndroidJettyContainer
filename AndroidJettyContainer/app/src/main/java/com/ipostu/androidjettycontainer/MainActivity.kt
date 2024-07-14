package com.ipostu.androidjettycontainer


import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    companion object {
        const val PREFS_NAME = "ExternalStoragePrefs"
        const val KEY_SELECTED_FOLDER_URI = "selected_folder_uri"
    }

    private lateinit var prefs: SharedPreferences
    private lateinit var selectFolderButton: Button
    private lateinit var folderPickerLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        selectFolderButton = findViewById(R.id.button)
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)

        folderPickerLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    result.data?.data?.let { uri ->
                        val takeFlags: Int =
                            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                        contentResolver.takePersistableUriPermission(uri, takeFlags)
                        prefs.edit().putString(KEY_SELECTED_FOLDER_URI, uri.toString()).apply()
                        Toast.makeText(this, "Folder selected: $uri", Toast.LENGTH_LONG).show()
                    }
                }
            }

        selectFolderButton.setOnClickListener {
            if (isPermissionGranted()) {
                openFolderPicker()
            } else {
                requestStoragePermissions()
            }
        }

        // Check if a folder is already selected
        val selectedFolderUri = prefs.getString(KEY_SELECTED_FOLDER_URI, null)
        if (selectedFolderUri != null) {
            // Use the selected folder URI as needed
            Toast.makeText(this, "Selected folder: $selectedFolderUri", Toast.LENGTH_LONG).show()
        }
    }

    private fun isPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestStoragePermissions() {
        requestPermissions(
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ), 1
        )
    }

    private fun openFolderPicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
        folderPickerLauncher.launch(intent)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openFolderPicker()
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
}