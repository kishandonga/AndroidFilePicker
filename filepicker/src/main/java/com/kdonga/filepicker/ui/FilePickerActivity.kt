package com.kdonga.filepicker.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.view.MenuItem
import com.kdonga.filepicker.R
import com.kdonga.filepicker.callback.OnFilePickerAction
import com.kdonga.filepicker.fragment.DirectoryFragment
import com.kdonga.filepicker.widget.FilePicker
import java.io.File

class FilePickerActivity : AppCompatActivity() {

    private lateinit var dirFrg: DirectoryFragment
    private var defaultTitle = ""
    private var pickerAction: OnFilePickerAction? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_picker)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        defaultTitle = FilePicker.builder.title
        pickerAction = FilePicker.pickerAction
        title = defaultTitle

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
        dirFrg.onFrgDestroy()
        super.onDestroy()
    }

    override fun onBackPressed() {
        if (dirFrg.onFrgBackPressed()) {
            super.onBackPressed()
        }
    }
}
