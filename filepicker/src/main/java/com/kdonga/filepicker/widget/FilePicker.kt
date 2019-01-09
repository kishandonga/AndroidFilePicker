package com.kdonga.filepicker.widget

import android.content.Context
import android.content.Intent
import com.kdonga.filepicker.callback.OnFilePickerAction
import com.kdonga.filepicker.ui.FilePickerActivity
import com.kdonga.filepicker.utility.SizeUnit

class FilePicker private constructor(builder: Builder) {

    companion object {
        internal var pickerAction: OnFilePickerAction? = null
        internal lateinit var builder: Builder
    }

    init {
        FilePicker.builder = builder
    }

    fun setOnFilePickerAction(pickerAction: OnFilePickerAction?) {
        FilePicker.pickerAction = pickerAction
    }

    fun start(context: Context) {
        context.startActivity(Intent(context, FilePickerActivity::class.java))
    }

    class Builder {

        internal var allowedFileExtension = listOf<String>()
        internal var title: String = "MY FILES"
        internal var fileSelectionSizeLimit: Long = 0L
        internal var sizeLimitErrorMessage: String = ""
        internal var showNotes: String = ""

        fun setAllowedFileExtension(filesExt: List<String>): Builder {
            allowedFileExtension = filesExt
            return this
        }

        fun setDefaultTitle(title: String): Builder {
            this.title = title
            return this
        }

        fun setMaxFileSelectionSize(value: Int, unit: SizeUnit, errorMessage: String): Builder {
            fileSelectionSizeLimit = value * unit.inBytes()
            sizeLimitErrorMessage = errorMessage
            return this
        }

        fun showDefaultNote(note: String): Builder {
            this.showNotes = note
            return this
        }

        fun build(): FilePicker {
            return FilePicker(this)
        }
    }
}