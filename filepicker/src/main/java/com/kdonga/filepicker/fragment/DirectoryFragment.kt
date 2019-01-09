package com.kdonga.filepicker.fragment

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Environment
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v7.widget.RecyclerView
import android.view.*
import android.view.animation.AnimationUtils
import com.kdonga.filepicker.R
import com.kdonga.filepicker.adapter.FileListAdapter
import com.kdonga.filepicker.model.HistoryEntry
import com.kdonga.filepicker.model.ListItemModel
import com.kdonga.filepicker.utility.SizeUnit
import com.kdonga.filepicker.utility.Utils.compareExtension
import com.kdonga.filepicker.utility.Utils.getRootSubtitle
import com.kdonga.filepicker.utility.extension
import com.kdonga.filepicker.widget.FilePicker
import kotlinx.android.synthetic.main.fp_frg_document_select_layout.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class DirectoryFragment : Fragment() {

    private lateinit var listAdapter: FileListAdapter
    private var receiverRegistered = false
    private var title: String = ""
    private var currentDir: File? = null
    private var action: OnDocumentSelectAction? = null
    private val items = ArrayList<ListItemModel>()
    private val history = ArrayList<HistoryEntry>()
    private val dateFormat = SimpleDateFormat("dd MMM HH:mm a", Locale.US)
    private var showHiddenFiles = false
    private val imageFileExt = arrayListOf("jpg", "png", "gif", "jpeg")

    private val sizeLimit = FilePicker.builder.fileSelectionSizeLimit
    private val allowedFileType = FilePicker.builder.allowedFileExtension
    private val fileSizeErrorMsg = FilePicker.builder.sizeLimitErrorMessage

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

    fun onFrgBackPressed(): Boolean {
        return if (history.size > 0) {
            val he = history.removeAt(history.size - 1)
            title = he.title
            action?.onTitleUpdate(title)
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

    fun onFrgDestroy() {
        if (receiverRegistered) {
            activity?.unregisterReceiver(receiver)
        }
    }

    fun setOnDocumentSelectAction(action: OnDocumentSelectAction) {
        this.action = action
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        registerReceiver()
        return inflater.inflate(R.layout.fp_frg_document_select_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val note = FilePicker.builder.showNotes
        if (note.isNotEmpty() && note.isNotBlank()) {
            tvInfoMsgText.text = note
            llInfoView.visibility = View.VISIBLE
        }

        listAdapter = FileListAdapter(items)
        rvFiles.adapter = listAdapter

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
                    he.title = title
                    action?.onTitleUpdate(title)
                    if (!listFiles(file, R.anim.fp_layout_anim_from_right)) {
                        return
                    }
                    history.add(he)
                    title = item.title
                    action?.onTitleUpdate(title)

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

                    action?.onFileSelected(file)
                }
            }
        })

        listRoots(R.anim.fp_layout_anim_from_right)
    }

    private fun applyLayoutAnimation(recyclerView: RecyclerView, animation: Int) {
        recyclerView.layoutAnimation = AnimationUtils.loadLayoutAnimation(recyclerView.context, animation)
        recyclerView.scheduleLayoutAnimation()
        listAdapter.notifyDataSetChanged()
    }

    private fun registerReceiver() {
        if (!receiverRegistered) {
            receiverRegistered = true
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
            activity?.registerReceiver(receiver, filter)
        }
    }

    private fun readFile(fileName: String): List<String> = File(fileName).bufferedReader().readLines()

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
            ext.subTitle = getRootSubtitle(defaultPath)
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
                            item.subTitle = getRootSubtitle(path)
                            item.file = File(path)
                            items.add(item)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }

        applyLayoutAnimation(rvFiles, animation)
    }

    private fun listFiles(dir: File?, animation: Int): Boolean {
        currentDir = dir
        items.clear()

        try {
            val fileList = dir?.listFiles()
            val files = getFilterFiles(fileList)

            if (files.isEmpty()) {
                rvFiles.visibility = View.GONE
                llEmptyView.visibility = View.VISIBLE
            } else {
                llEmptyView.visibility = View.GONE
                rvFiles.visibility = View.VISIBLE
            }

            Arrays.sort(files) { lhs, rhs ->
                if (lhs.isDirectory != rhs.isDirectory) {
                    if (lhs.isDirectory) -1 else 1
                } else lhs.name.compareTo(rhs.name, ignoreCase = true)
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

                    if (compareExtension(imageFileExt, ext)) {
                        item.thumbFilePath = file.absolutePath
                    }

                    items.add(item)
                }
            }

            applyLayoutAnimation(rvFiles, animation)
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
                    if (compareExtension(allowedFileType, file.name.extension())) {
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

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.fp_menu, menu)
        val checkBox = menu?.getItem(0) as MenuItem
        checkBox.setOnMenuItemClickListener {
            checkBox.isChecked = !checkBox.isChecked
            showHiddenFiles = checkBox.isChecked
            if (title.startsWith(".")) {
                onFrgBackPressed()
            }
            refreshFileDir()
            return@setOnMenuItemClickListener true
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun showMessage(error: String) {
        Snackbar.make(rootLayout, error, Snackbar.LENGTH_SHORT).show()
    }

    interface OnDocumentSelectAction {
        fun onFileSelected(file: File)

        fun onTitleUpdate(name: String)
    }
}
