package com.kdonga.filepicker.utility

import android.support.v7.widget.RecyclerView
import android.view.View

class EmptyObserver(private val recyclerView: RecyclerView, private val emptyView: View) : RecyclerView.AdapterDataObserver() {

    init {
        checkIfEmpty()
    }

    private fun checkIfEmpty() {
        recyclerView.adapter?.let {
            val visible = it.itemCount == 0
            emptyView.visibility = if (visible) View.VISIBLE else View.GONE
            recyclerView.visibility = if (visible) View.GONE else View.VISIBLE
        }
    }

    override fun onChanged() {
        checkIfEmpty()
    }

    override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
        checkIfEmpty()
    }

    override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
        checkIfEmpty()
    }
}