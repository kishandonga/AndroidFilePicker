package com.kdonga.filepicker.fragment

import android.animation.Animator
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.support.v4.app.Fragment
import android.support.v7.widget.RecyclerView
import android.view.*
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import com.kdonga.filepicker.R
import com.kdonga.filepicker.adapter.FileListAdapter
import com.kdonga.filepicker.model.HistoryEntry
import com.kdonga.filepicker.model.ListItemModel
import com.kdonga.filepicker.utility.Utils.collapseView
import com.kdonga.filepicker.utility.Utils.compareExtension
import com.kdonga.filepicker.utility.Utils.expandView
import com.kdonga.filepicker.utility.Utils.formatFileSize
import com.kdonga.filepicker.utility.Utils.getRootSubtitle
import com.kdonga.filepicker.utility.extension
import kotlinx.android.synthetic.main.frg_document_select_layout.*
import kotlinx.android.synthetic.main.frg_document_select_layout.view.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class DirectoryFragment : Fragment() {

    private var receiverRegistered = false
    private var title: String = ""
    private var currentDir: File? = null
    private lateinit var rvFileList: RecyclerView
    private lateinit var listAdapter: FileListAdapter
    private lateinit var llEmptyView: LinearLayout
    private var action: OnDocumentSelectAction? = null
    private val items = ArrayList<ListItemModel>()
    private val history = ArrayList<HistoryEntry>()
    private val sizeLimit = (1024 * 1024 * 1024).toLong()
    private val dateFormat = SimpleDateFormat("dd MMM HH:mm a", Locale.US)
    private val allowedFileType = arrayListOf("jpg", "png", "gif", "jpeg") //ArrayList<String>()
    private var showHiddenFiles = false
    private val handler: Handler = Handler(Looper.getMainLooper())
    private val imageFileExt = arrayListOf("jpg", "png", "gif", "jpeg")

    private val receiver = object : BroadcastReceiver() {

        override fun onReceive(arg0: Context, intent: Intent) {
            val r = Runnable {
                refreshFileDir()
            }

            if (Intent.ACTION_MEDIA_UNMOUNTED == intent.action) {
                rvFileList.postDelayed(r, 1000)
            } else {
                r.run()
            }
        }
    }

    private fun refreshFileDir() {
        try {
            if (currentDir == null) {
                listRoots(R.anim.layout_anim_from_bottom)
            } else {
                listFiles(currentDir, R.anim.layout_anim_from_bottom)
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
                listFiles(he.dir, R.anim.layout_anim_from_left)
            } else {
                listRoots(R.anim.layout_anim_from_left)
            }
            rvFileList.scrollToPosition(he.scrollToPosition)
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
        return inflater.inflate(R.layout.frg_document_select_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        llEmptyView = view.llEmptyView!!
        rvFileList = view.rvFileList!!

        listAdapter = FileListAdapter(items)
        rvFileList.adapter = listAdapter

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
                    if (!listFiles(file, R.anim.layout_anim_from_right)) {
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
                        showMessage("FileUploadLimit")
                        return
                    }

                    if (file.length() == 0L) {
                        return
                    }

                    action?.onFileSelected(file.absolutePath)
                }
            }
        })

        btnCloseInfoView.setOnClickListener {
            handler.removeCallbacksAndMessages(null)
            collapseView(llInfoView)
        }

        listRoots(R.anim.layout_anim_from_right)
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
                ext.icon = R.drawable.ic_external_storage
            } else {
                ext.title = "Internal Storage"
                ext.icon = R.drawable.ic_phone_storage
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
                            item.icon = R.drawable.ic_external_storage
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

        applyLayoutAnimation(rvFileList, animation)
    }

    private fun listFiles(dir: File?, animation: Int): Boolean {
        currentDir = dir
        items.clear()

        try {
            val fileList = dir?.listFiles()
            val files = getFilterFiles(fileList)

            if (files.isEmpty()) {
                rvFileList.visibility = View.GONE
                llEmptyView.visibility = View.VISIBLE
            } else {
                llEmptyView.visibility = View.GONE
                rvFileList.visibility = View.VISIBLE
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
                    item.icon = R.drawable.ic_directory
                    item.dateTime = dateFormat.format(Date(file.lastModified()))
                    item.totalSubItem = getFilterFiles(file.listFiles()).size
                    items.add(item)

                } else {
                    val ext = file.name.extension()

                    item.isFile = true
                    item.extension = ext.toUpperCase()
                    item.dateTime = dateFormat.format(Date(file.lastModified()))
                    item.fileSize = formatFileSize(file.length())

                    if (compareExtension(imageFileExt, ext)) {
                        item.thumbFilePath = file.absolutePath
                    }

                    items.add(item)
                }
            }

            applyLayoutAnimation(rvFileList, animation)
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
        inflater?.inflate(R.menu.menu_file_explorer, menu)
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

    fun showMessage(error: String) {
        tvInfoMsgText.text = error
        val anim = expandView(llInfoView)
        anim.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(p0: Animator?) {}

            override fun onAnimationEnd(p0: Animator?) {
                handler.postDelayed({ collapseView(llInfoView) }, 3000)
            }

            override fun onAnimationCancel(p0: Animator?) {}

            override fun onAnimationStart(p0: Animator?) {}
        })
    }

    interface OnDocumentSelectAction {
        fun onFileSelected(path: String)

        fun onTitleUpdate(name: String)
    }
}
