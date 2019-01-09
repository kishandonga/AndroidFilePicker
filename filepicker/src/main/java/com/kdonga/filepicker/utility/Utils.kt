package com.kdonga.filepicker.utility

import android.os.StatFs


object Utils {

    fun getRootSubtitle(path: String): String {
        val stat = StatFs(path)
        val total = stat.blockCountLong * stat.blockSizeLong
        val free = stat.availableBlocksLong * stat.blockSizeLong
        return if (total == 0L) {
            ""
        } else SizeUnit.formatFileSize(free) + " / " + SizeUnit.formatFileSize(total)
    }

    fun compareExtension(extTypes: List<String>, desired: String): Boolean {
        for (str in extTypes) {
            if (desired == str) {
                return true
            }
        }
        return false
    }
}
