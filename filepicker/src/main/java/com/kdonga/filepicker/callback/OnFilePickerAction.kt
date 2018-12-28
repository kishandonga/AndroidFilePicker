package com.kdonga.filepicker.callback

import java.io.File

interface OnFilePickerAction {
    fun onFileSelected(file: File)
}