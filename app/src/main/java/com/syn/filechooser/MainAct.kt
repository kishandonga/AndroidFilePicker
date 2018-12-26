package com.syn.filechooser

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.kdonga.filepicker.ui.FilePickerAct
import kotlinx.android.synthetic.main.activity_main.*

class MainAct : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnStart.setOnClickListener {
            startActivity(Intent(this, FilePickerAct::class.java))
        }
    }
}
