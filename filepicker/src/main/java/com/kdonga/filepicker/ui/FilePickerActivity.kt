package com.kdonga.filepicker.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Environment
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.animation.AnimationUtils
import com.kdonga.filepicker.R
import com.kdonga.filepicker.adapter.FileListAdapter
import com.kdonga.filepicker.callback.OnFilePickerAction
import com.kdonga.filepicker.model.HistoryEntry
import com.kdonga.filepicker.model.ListItemModel
import com.kdonga.filepicker.utility.SizeUnit
import com.kdonga.filepicker.utility.Utils
import com.kdonga.filepicker.utility.extension
import com.kdonga.filepicker.utility.setEmptyView
import com.kdonga.filepicker.widget.FilePicker
import kotlinx.android.synthetic.main.fp_activity_file_picker.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.properties.Delegates

class FilePickerActivity : AppCompatActivity() {

    private var listAdapter: FileListAdapter = FileListAdapter()
    private var pickerAction: OnFilePickerAction? = null
    private var currentDir: File? = null
    private val items = ArrayList<ListItemModel>()
    private val history = ArrayList<HistoryEntry>()
    private val dateFormat = SimpleDateFormat("dd MMM HH:mm a", Locale.US)
    private var showHiddenFiles = false
    private val imageFileExt = arrayListOf("jpg", "png", "gif", "jpeg")
    private var sizeLimit: Long = 0L
    private var allowedFileType = listOf<String>()
    private var fileSizeErrorMsg: String = ""
    private var defaultTitle = "MY FILES"
    private var titleUpdate: String by Delegates.observable(defaultTitle) { _, _, new ->
        title = if (new.isNotBlank() && new.isNotEmpty()) {
            new
        } else {
            defaultTitle
        }
    }

    private val receiver = object : BroadcastReceiver() {

        override fun onReceive(arg0: Context, intent: Intent) {
            val r = Runnable {
                refreshFileDir()
            }

            if (Intent.ACTION_MEDIA_UNMOUNTED == intent.action) {
                rvFiles.postDelayed(r, 1000)
            } else {
                r.run()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fp_activity_file_picker)

        initView()

        listAdapter.setItems(items)
        rvFiles.adapter = listAdapter
        rvFiles.setEmptyView(llEmptyView)

        listAdapter.setOnItemClickListener(object : FileListAdapter.OnItemClickListener {
            override fun onItemClick(item: ListItemModel, position: Int) {

                if (position < 0 || position >= items.size) {
                    return
                }

                val file = item.file
                if (file.isDirectory) {

                    val he = HistoryEntry()
                    he.scrollToPosition = position
                    he.dir = currentDir
                    he.title = titleUpdate
                    if (!listFiles(file, R.anim.fp_layout_anim_from_right)) {
                        return
                    }
                    history.add(he)
                    titleUpdate = item.title

                } else {

                    if (!file.canRead()) {
                        showMessage("AccessError")
                        return
                    }
                    if (file.length() > sizeLimit) {
                        showMessage(fileSizeErrorMsg)
                        return
                    }
                    if (file.length() == 0L) {
                        return
                    }

                    pickerAction?.onFileSelected(file)
                }
            }
        })

        listRoots(R.anim.fp_layout_anim_from_right)
    }

    private fun initView() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        FilePicker.getInstance().getBuilder()?.let {
            titleUpdate = it.title
            pickerAction = it.pickerAction

            sizeLimit = it.fileSelectionSizeLimit
            allowedFileType = it.allowedFileExtension
            fileSizeErrorMsg = it.sizeLimitErrorMessage
        }
    }

    private fun refreshFileDir() {
        try {
            if (currentDir == null) {
                listRoots(R.anim.fp_layout_anim_from_bottom)
            } else {
                listFiles(currentDir, R.anim.fp_layout_anim_from_bottom)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun hasItemInHistory(): Boolean {
        return if (history.size > 0) {
            val he = history.removeAt(history.size - 1)
            titleUpdate = he.title
            if (he.dir != null) {
                listFiles(he.dir, R.anim.fp_layout_anim_from_left)
            } else {
                listRoots(R.anim.fp_layout_anim_from_left)
            }
            rvFiles.scrollToPosition(he.scrollToPosition)
            false
        } else {
            true
        }
    }

    private fun applyLayoutAnimation(animation: Int) {
        rvFiles.layoutAnimation = AnimationUtils.loadLayoutAnimation(rvFiles.context, animation)
        rvFiles.scheduleLayoutAnimation()
        listAdapter.setItems(items)
    }

    private fun registerReceiver() {
        val filter = IntentFilter()
        filter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL)
        filter.addAction(Intent.ACTION_MEDIA_CHECKING)
        filter.addAction(Intent.ACTION_MEDIA_EJECT)
        filter.addAction(Intent.ACTION_MEDIA_MOUNTED)
        filter.addAction(Intent.ACTION_MEDIA_NOFS)
        filter.addAction(Intent.ACTION_MEDIA_REMOVED)
        filter.addAction(Intent.ACTION_MEDIA_SHARED)
        filter.addAction(Intent.ACTION_MEDIA_UNMOUNTABLE)
        filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED)
        filter.addDataScheme("file")
        registerReceiver(receiver, filter)
    }

    private fun listRoots(animation: Int) {
        currentDir = null
        items.clear()

        val paths = HashSet<String>()
        val defaultPath = Environment.getExternalStorageDirectory().path
        val defaultPathState = Environment.getExternalStorageState()
        if (defaultPathState == Environment.MEDIA_MOUNTED || defaultPathState == Environment.MEDIA_MOUNTED_READ_ONLY) {
            val ext = ListItemModel()
            if (Environment.isExternalStorageRemovable()) {
                ext.title = "SdCard"
                ext.icon = R.drawable.fp_ic_external_storage
            } else {
                ext.title = "Internal Storage"
                ext.icon = R.drawable.fp_ic_phone_storage
            }
            ext.subTitle = Utils.getRootSubtitle(defaultPath)
            ext.file = Environment.getExternalStorageDirectory()
            items.add(ext)
            paths.add(defaultPath)
        }

        for (line in readFile("/proc/mounts")) {
            if (line.contains("vfat") || line.contains("/mnt")) {

                val tokens = StringTokenizer(line, " ")
                tokens.nextToken()
                var path = tokens.nextToken()
                if (paths.contains(path)) {
                    continue
                }
                if (line.contains("/dev/block/vold")) {

                    if (!line.contains("/mnt/secure") &&
                            !line.contains("/mnt/asec") &&
                            !line.contains("/mnt/obb") &&
                            !line.contains("/dev/mapper") &&
                            !line.contains("tmpfs")) {

                        if (!File(path).isDirectory) {
                            val index = path.lastIndexOf('/')
                            if (index != -1) {
                                val newPath = "/storage/" + path.substring(index + 1)
                                if (File(newPath).isDirectory) {
                                    path = newPath
                                }
                            }
                        }
                        paths.add(path)

                        try {
                            val item = ListItemModel()
                            if (path.toLowerCase().contains("sd")) {
                                item.title = "SdCard"
                            } else {
                                item.title = "External Storage"
                            }
                            item.icon = R.drawable.fp_ic_external_storage
                            item.subTitle = Utils.getRootSubtitle(path)
                            item.file = File(path)
                            items.add(item)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }

        applyLayoutAnimation(animation)
    }

    private fun listFiles(dir: File?, animation: Int): Boolean {
        currentDir = dir
        items.clear()

        try {
            val fileList = dir?.listFiles()
            val files = getFilterFiles(fileList)

            Arrays.sort(files) { lhs, rhs ->
                if (lhs.isDirectory != rhs.isDirectory) {
                    if (lhs.isDirectory) -1 else 1
                } else {
                    lhs.name.compareTo(rhs.name, ignoreCase = true)
                }
            }

            for (file in files) {

                val item = ListItemModel()
                item.title = file.name
                item.file = file

                if (file.isDirectory) {
                    item.isDirectory = true
                    item.icon = R.drawable.fp_ic_directory
                    item.dateTime = dateFormat.format(Date(file.lastModified()))
                    item.totalSubItem = getFilterFiles(file.listFiles()).size
                    items.add(item)

                } else {
                    val ext = file.name.extension()

                    item.isFile = true
                    item.extension = ext.toUpperCase()
                    item.dateTime = dateFormat.format(Date(file.lastModified()))
                    item.fileSize = SizeUnit.formatFileSize(file.length())

                    if (Utils.compareExtension(imageFileExt, ext)) {
                        item.thumbFilePath = file.absolutePath
                    }

                    items.add(item)
                }
            }

            applyLayoutAnimation(animation)
            return true

        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    private fun getFilterFiles(files: Array<File>?): Array<File> {
        val filesList = ArrayList<File>()
        files?.let {

            val extList = ArrayList<File>()

            for (file in it) {
                if (!file.isDirectory) {
                    if (Utils.compareExtension(allowedFileType, file.name.extension())) {
                        extList.add(file)
                    }
                } else {
                    extList.add(file)
                }
            }

            for (file in extList) {
                if (!showHiddenFiles) {
                    if (!file.name.startsWith(".")) {
                        filesList.add(file)
                    }
                } else {
                    filesList.add(file)
                }
            }
        }
        return filesList.toTypedArray()
    }

    private fun showMessage(error: String) {
        Snackbar.make(rootLayout, error, Snackbar.LENGTH_SHORT).show()
    }

    private fun readFile(fileName: String): List<String> = File(fileName).bufferedReader().readLines()

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater?.inflate(R.menu.fp_menu, menu)
        val checkBox = menu?.getItem(0) as MenuItem
        checkBox.setOnMenuItemClickListener {
            checkBox.isChecked = !checkBox.isChecked
            showHiddenFiles = checkBox.isChecked
            if (titleUpdate.startsWith(".")) {
                hasItemInHistory()
            }
            refreshFileDir()
            return@setOnMenuItemClickListener true
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        registerReceiver()
        super.onResume()
    }

    override fun onPause() {
        unregisterReceiver(receiver)
        super.onPause()
    }

    override fun onBackPressed() {
        if (hasItemInHistory()) {
            super.onBackPressed()
        }
    }
}
