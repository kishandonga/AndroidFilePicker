package com.kdonga.filepicker.adapter

import android.graphics.BitmapFactory
import android.media.ThumbnailUtils
import android.support.v7.widget.AppCompatImageView
import android.support.v7.widget.AppCompatTextView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.kdonga.filepicker.R
import com.kdonga.filepicker.model.ListItemModel
import kotlinx.android.synthetic.main.fp_list_document.view.*
import java.util.*

class FileListAdapter : RecyclerView.Adapter<FileListAdapter.ViewHolder>() {

    private var listener: OnItemClickListener? = null
    private var items: ArrayList<ListItemModel> = arrayListOf()

    fun setItems(items: ArrayList<ListItemModel>) {
        this.items = items
        notifyDataSetChanged()
    }

    fun setOnItemClickListener(onItemClickListener: OnItemClickListener) {
        this.listener = onItemClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, i: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.fp_list_document, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, i: Int) {
        holder.bindTo(items[i], listener)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    interface OnItemClickListener {
        fun onItemClick(item: ListItemModel, position: Int)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private lateinit var tvExtension: AppCompatTextView
        private lateinit var ivThumb: AppCompatImageView
        private lateinit var tvTitle: AppCompatTextView
        private lateinit var tvSubTitle: AppCompatTextView
        private lateinit var tvSizeAndItem: AppCompatTextView

        init {
            initView(itemView)
        }

        private fun initView(rootView: View) {
            tvExtension = rootView.tvExtension!!
            ivThumb = rootView.ivThumb!!
            tvTitle = rootView.tvTitle!!
            tvSubTitle = rootView.tvSubTitle!!
            tvSizeAndItem = rootView.tvSizeAndItem!!
        }

        private fun resetView() {
            tvExtension.visibility = View.VISIBLE
            ivThumb.visibility = View.VISIBLE
            tvTitle.visibility = View.VISIBLE
            tvSubTitle.visibility = View.VISIBLE
            tvSizeAndItem.visibility = View.VISIBLE
        }

        fun bindTo(item: ListItemModel, listener: OnItemClickListener?) {
            resetView()
            tvTitle.text = item.title

            if (item.isFile) {

                tvSubTitle.text = item.dateTime
                tvSizeAndItem.text = item.fileSize

                if (item.thumbFilePath != null) {
                    val resized = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(item.thumbFilePath), 256, 256)
                    ivThumb.setImageBitmap(resized)
                    tvExtension.visibility = View.GONE
                } else {
                    ivThumb.visibility = View.GONE
                    tvExtension.text = item.extension
                }
            } else if (item.isDirectory) {

                tvExtension.visibility = View.GONE
                tvSubTitle.text = item.dateTime
                ivThumb.setImageResource(item.icon)
                val grm: String = if (item.totalSubItem > 1) {
                    " Items"
                } else {
                    " Item"
                }
                tvSizeAndItem.text = String.format("%d %s", item.totalSubItem, grm)

            } else {
                ivThumb.setImageResource(item.icon)
                tvSubTitle.text = item.subTitle
                tvExtension.visibility = View.GONE
                tvSizeAndItem.visibility = View.GONE
            }

            itemView.setOnClickListener { listener?.onItemClick(item, adapterPosition) }
        }
    }
}