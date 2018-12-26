package com.kdonga.filepicker.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.view.MenuItem
import com.kdonga.filepicker.R
import com.kdonga.filepicker.fragment.DirectoryFragment

class FilePickerAct : AppCompatActivity() {

    private lateinit var dirFrg: DirectoryFragment
    private val defaultTitle = "MY FILES"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_picker)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = defaultTitle

        dirFrg = DirectoryFragment()
        dirFrg.setOnDocumentSelectAction(object : DirectoryFragment.OnDocumentSelectAction {

            override fun onFileSelected(path: String) {
                dirFrg.showMessage(path)
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
