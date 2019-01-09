package com.kdonga.filepicker.ui

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.view.MenuItem
import android.widget.Toast
import com.kdonga.filepicker.R
import com.kdonga.filepicker.callback.OnFilePickerAction
import com.kdonga.filepicker.fragment.DirectoryFragment
import com.kdonga.filepicker.widget.FilePicker
import java.io.File


class FilePickerActivity : AppCompatActivity() {

    private lateinit var dirFrg: DirectoryFragment
    private lateinit var permissionStatus: SharedPreferences
    private var defaultTitle = ""
    private var pickerAction: OnFilePickerAction? = null

    private val externalStoragePermissionConstant = 100
    private val requestPermissionSetting = 101
    private var sentToSettings = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fp_activity_file_picker)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        defaultTitle = FilePicker.builder.title
        pickerAction = FilePicker.pickerAction
        title = defaultTitle

        permissionStatus = getSharedPreferences("FilePickerPermissionStatus", MODE_PRIVATE)

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                //Show Information about why you need the permission
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Need Storage Permission")
                builder.setMessage("This app needs storage permission.")
                builder.setPositiveButton("Grant") { dialog, _ ->
                    dialog.cancel()
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), externalStoragePermissionConstant)
                }
                builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
                builder.show()
            } else if (permissionStatus.getBoolean(Manifest.permission.READ_EXTERNAL_STORAGE, false)) {
                //Previously Permission Request was cancelled with 'Don't Ask Again',
                // Redirect to Settings after showing Information about why you need the permission
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Need Storage Permission")
                builder.setMessage("This app needs storage permission.")
                builder.setPositiveButton("Grant") { dialog, _ ->
                    dialog.cancel()
                    sentToSettings = true
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivityForResult(intent, requestPermissionSetting)
                    Toast.makeText(baseContext, "Go to Permissions to Grant Storage", Toast.LENGTH_LONG).show()
                }
                builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
                builder.show()
            } else {
                //just request the permission
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), externalStoragePermissionConstant)
            }

            val editor = permissionStatus.edit()
            editor.putBoolean(Manifest.permission.READ_EXTERNAL_STORAGE, true)
            editor.apply()
        } else {
            //You already have the permission, just go ahead.
            proceedAfterPermission()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == externalStoragePermissionConstant) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //The External Storage Write Permission is granted to you... Continue your left job...
                proceedAfterPermission()
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    //Show Information about why you need the permission
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle("Need Storage Permission")
                    builder.setMessage("This app needs storage permission")
                    builder.setPositiveButton("Grant") { dialog, _ ->
                        dialog.cancel()
                        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), externalStoragePermissionConstant)
                    }
                    builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
                    builder.show()
                } else {
                    Toast.makeText(baseContext, "Unable to get Permission", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == requestPermissionSetting) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                //Got Permission
                proceedAfterPermission()
            }
        }
    }

    override fun onPostResume() {
        super.onPostResume()
        if (sentToSettings) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                //Got Permission
                proceedAfterPermission()
            }
        }
    }

    private fun proceedAfterPermission() {
        dirFrg = DirectoryFragment()
        dirFrg.setOnDocumentSelectAction(object : DirectoryFragment.OnDocumentSelectAction {

            override fun onFileSelected(file: File) {
                pickerAction?.onFileSelected(file)
            }

            override fun onTitleUpdate(name: String) {
                title = if (!TextUtils.isEmpty(name)) {
                    name
                } else {
                    defaultTitle
                }
            }
        })

        val manager = supportFragmentManager
        val transaction = manager.beginTransaction()
        transaction.add(R.id.flContainer, dirFrg, DirectoryFragment::class.java.name)
        transaction.commit()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        if (::dirFrg.isInitialized) {
            dirFrg.onFrgDestroy()
        }
        super.onDestroy()
    }

    override fun onBackPressed() {
        if (::dirFrg.isInitialized) {
            if (dirFrg.onFrgBackPressed()) {
                super.onBackPressed()
            }
        } else {
            super.onBackPressed()
        }
    }
}
