package com.rsinukov.shutterstockclient.search.ui

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.rsinukov.shutterstockclient.R
import com.rsinukov.shutterstockclient.search.bl.ImageViewModel
import com.rsinukov.shutterstockclient.utils.adapter.DelegatedAdapter
import com.rsinukov.shutterstockclient.utils.adapter.EmptyViewHolder
import com.rsinukov.shutterstockclient.utils.adapter.TypedAdapterDelegate
import com.rsinukov.shutterstockclient.utils.adapter.ViewHolder
import com.rsinukov.shutterstockclient.utils.ui.bindView

class SearchAdapter(
    private val context: Context,
    private val onErrorClickListener: OnErrorClickListener
) : DelegatedAdapter() {

    private companion object {
        const val TYPE_IMAGE = 0
        const val TYPE_LOAD_MORE = 1
        const val TYPE_ERROR = 2
        const val TYPE_EMPTY = 3
    }

    override var items: List<Any> = emptyList()

    init {
        addDelegate(TYPE_LOAD_MORE, TypedAdapterDelegate { parent ->
            val layout = LayoutInflater.from(context).inflate(R.layout.item_loading, parent, false)
            EmptyViewHolder<LoadMoreItem>(layout)
        })
        addDelegate(TYPE_EMPTY, TypedAdapterDelegate { parent ->
            val layout = LayoutInflater.from(context).inflate(R.layout.item_empty, parent, false)
            EmptyViewHolder<EmptyItem>(layout)
        })
        addDelegate(TYPE_ERROR, TypedAdapterDelegate { parent ->
            val layout = LayoutInflater.from(context).inflate(R.layout.item_error, parent, false)
            layout.setOnClickListener { onErrorClickListener.invoke() }
            EmptyViewHolder<ErrorItem>(layout)
        })
        addDelegate(TYPE_IMAGE, TypedAdapterDelegate { parent ->
            val layout = LayoutInflater.from(context).inflate(R.layout.item_image, parent, false)
            ImageViewHolder(layout)
        })
    }

    override fun getItemViewType(position: Int): Int = when (items[position]) {
        is LoadMoreItem -> TYPE_LOAD_MORE
        is ErrorItem -> TYPE_ERROR
        is ImageViewModel -> TYPE_IMAGE
        is EmptyItem -> TYPE_EMPTY
        else -> throw IllegalStateException("Unknown item: ${items[position]}")
    }

    fun setItems(images: List<ImageViewModel>, moreState: MoreState) {
        val newItems = mutableListOf<Any>()
        newItems.addAll(images)

        when (moreState) {
            MoreState.HasMore -> newItems.add(LoadMoreItem)
            MoreState.Error -> newItems.add(ErrorItem)
            MoreState.Full -> if (images.isEmpty()) newItems.add(EmptyItem) else Unit
        }

        items = newItems

        // items are only added to the end or cleared, no need for diff utils
        notifyDataSetChanged()
    }

    object LoadMoreItem
    object ErrorItem
    object EmptyItem

    enum class MoreState { HasMore, Full, Error }
}

class ImageViewHolder(view: View) : ViewHolder<ImageViewModel>(view) {

    private val image: ImageView by bindView(R.id.item_image_image)
    private val title: TextView by bindView(R.id.item_image_description)

    override fun bind(data: ImageViewModel) {
        val ratio = "${data.aspect}:1"
        (image.layoutParams as ConstraintLayout.LayoutParams).dimensionRatio = ratio
        Glide.with(itemView.context)
            .load(data.previewUrl)
            .into(image)
        title.text = data.description
    }
}

typealias OnErrorClickListener = () -> Unit
