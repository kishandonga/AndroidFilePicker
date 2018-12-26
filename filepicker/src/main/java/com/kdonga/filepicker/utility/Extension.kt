package com.kdonga.filepicker.utility

import android.content.res.Resources

val Int.toDp: Int get() = (this / Resources.getSystem().displayMetrics.density).toInt()
val Int.toPx: Int get() = (this * Resources.getSystem().displayMetrics.density).toInt()

fun String.extension(): String {
    val sp = this.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
    return if (sp.size > 1) sp[sp.size - 1] else "?"
}