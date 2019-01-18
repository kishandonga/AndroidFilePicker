package com.syn.filechooser

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.kdonga.filepicker.callback.OnFilePickerAction
import com.kdonga.filepicker.utility.SizeUnit
import com.kdonga.filepicker.widget.FilePicker
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

class MainAct : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val file = FilePicker.Builder()
                .setAllowedFileExtension(arrayListOf("jpg", "png", "gif", "jpeg"))
                .setDefaultTitle("")
                .setMaxFileSelectionSize(5, SizeUnit.KB, "Please select file below 5KB")
                .setOnFilePickerAction(object : OnFilePickerAction {
                    override fun onFileSelected(file: File) {

                    }
                }).build()


        btnStart.setOnClickListener {
            file.start(this)
        }
    }
}
