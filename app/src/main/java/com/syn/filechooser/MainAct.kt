package com.syn.filechooser

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.kdonga.filepicker.utility.SizeUnit
import com.kdonga.filepicker.widget.FilePicker
import kotlinx.android.synthetic.main.activity_main.*

class MainAct : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val file = FilePicker()
                .setAllowedFileExtension(arrayListOf("jpg", "png", "gif", "jpeg"))
                .setDefaultTitle("")
                .setMaxFileSelectionSize(5, SizeUnit.KB, "Please select file below 5KB")

        btnStart.setOnClickListener {
            file.startFilePickerActivityForResult(this, 123)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }
}
