package com.kdonga.filepicker.model

import java.io.File

class ListItemModel {
    //Common field
    var icon = 0
    var title = ""
    var dateTime = ""
    lateinit var file: File

    //Root
    var subTitle = ""

    //Directory
    var isDirectory = false
    var totalSubItem = 0

    //File
    var isFile = false
    var extension = ""
    var thumbFilePath: String? = null
    var fileSize = ""
}