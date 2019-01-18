package com.kdonga.filepicker.widget

import android.content.Context
import android.content.Intent
import com.kdonga.filepicker.callback.OnFilePickerAction
import com.kdonga.filepicker.ui.FilePickerActivity
import com.kdonga.filepicker.utility.SizeUnit

class FilePicker private constructor() {

    companion object {
        @Volatile
        private var instance: FilePicker? = null

        internal fun getInstance() = instance ?: synchronized(this) {
            instance ?: FilePicker().also { instance = it }
        }
    }

    private var builder: Builder? = null

    fun start(context: Context) {
        if (builder == null) {
            throw RuntimeException("First initialize builder of the filepicker then after call the start method!")
        }
        context.startActivity(Intent(context, FilePickerActivity::class.java))
    }

    internal fun setBuilder(builder: Builder?) {
        this.builder = builder
    }

    internal fun getBuilder(): Builder? {
        return builder
    }

    class Builder {

        internal var allowedFileExtension = listOf<String>()
        internal var title: String = "MY FILES"
        internal var fileSelectionSizeLimit: Long = 0L
        internal var sizeLimitErrorMessage: String = ""
        internal var pickerAction: OnFilePickerAction? = null

        companion object {
            @Volatile
            private var instance: Builder = Builder()
        }

        fun setOnFilePickerAction(pickerAction: OnFilePickerAction?): Builder {
            instance.pickerAction = pickerAction
            return this
        }

        fun setAllowedFileExtension(filesExt: List<String>): Builder {
            instance.allowedFileExtension = filesExt
            return this
        }

        fun setDefaultTitle(title: String): Builder {
            instance.title = title
            return this
        }

        fun setMaxFileSelectionSize(value: Int, unit: SizeUnit, errorMessage: String): Builder {
            instance.fileSelectionSizeLimit = value * unit.inBytes()
            instance.sizeLimitErrorMessage = errorMessage
            return this
        }

        fun build(): FilePicker {
            val file = FilePicker.getInstance()
            file.setBuilder(Builder.instance)
            return file
        }
    }
}