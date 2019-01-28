package com.kdonga.filepicker.widget

import android.app.Activity
import android.content.Intent
import com.kdonga.filepicker.ui.FilePickerActivity
import com.kdonga.filepicker.utility.SizeUnit

class FilePicker {

    companion object {
        const val AllowedFileExt: String = "ALLOWED_FILE_EXT"
        const val DefaultTitle: String = "DF_TITLE"
        const val FileSizeLimit: String = "FILE_SIZE_LIMIT"
        const val SizeLimitErrorMsg: String = "SIZE_LIMIT_ERROR_MSG"
        const val SelectedFilePath: String = "SET_FILE_PATH"
    }

    private var allowedFileExtension = ArrayList<String>()
    private var defaultTitle: String = "MY FILES"
    private var fileSelectionSizeLimit: Long = 0L
    private var sizeLimitErrorMessage: String = ""

    fun setAllowedFileExtension(filesExt: ArrayList<String>): FilePicker {
        this.allowedFileExtension = filesExt
        return this
    }

    fun setDefaultTitle(title: String): FilePicker {
        this.defaultTitle = title
        return this
    }

    fun setMaxFileSelectionSize(value: Int, unit: SizeUnit, errorMessage: String): FilePicker {
        this.fileSelectionSizeLimit = value * unit.inBytes()
        this.sizeLimitErrorMessage = errorMessage
        return this
    }

    fun startFilePickerActivityForResult(activity: Activity, requestCode: Int) {
        val intent = Intent(activity, FilePickerActivity::class.java)
        intent.putExtra(AllowedFileExt, allowedFileExtension)
        intent.putExtra(DefaultTitle, defaultTitle)
        intent.putExtra(FileSizeLimit, fileSelectionSizeLimit)
        intent.putExtra(SizeLimitErrorMsg, sizeLimitErrorMessage)
        activity.startActivityForResult(intent, requestCode)
    }
}