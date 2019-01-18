package com.kdonga.filepicker.utility

import android.support.v7.widget.RecyclerView
import android.view.View

fun String.extension(): String {
    val sp = this.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
    return if (sp.size > 1) sp[sp.size - 1] else "?"
}

fun RecyclerView.setEmptyView(emptyView: View) {
    this.adapter?.registerAdapterDataObserver(EmptyObserver(this, emptyView))
}