package com.rsinukov.shutterstockclient.utils.adapter

import android.support.v7.widget.RecyclerView
import android.util.SparseArray
import android.view.View
import android.view.ViewGroup

abstract class DelegatedAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val delegates: SparseArray<AdapterDelegate> = SparseArray()

    abstract val items: List<Any>

    fun addDelegate(viewType: Int, delegate: AdapterDelegate) {
        delegates.put(viewType, delegate)
    }

    final override fun getItemCount() = items.size

    abstract override fun getItemViewType(position: Int): Int

    final override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val adapterDelegate = delegates[viewType]
            ?: throw IllegalStateException("No delegate for $viewType viewType")
        return adapterDelegate.onCreateViewHolder(parent)
    }

    final override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        delegates[getItemViewType(position)].onBindViewHolder(holder, position, items)
    }
}


interface AdapterDelegate {

    fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder

    fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, items: List<Any>)
}


class TypedAdapterDelegate<in Data>(
    private val creator: (ViewGroup) -> ViewHolder<Data>
) : AdapterDelegate {

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder<Data> {
        return creator(parent)
    }

    @Suppress("UNCHECKED_CAST")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, items: List<Any>) {
        val data = items[position] as? Data
            ?: throw IllegalStateException("Incorrect data for position $position. Was ${items[position]}. Check your items list.")
        (holder as ViewHolder<Data>).bind(data)
    }
}

abstract class ViewHolder<in Data>(itemView: View) : RecyclerView.ViewHolder(itemView) {

    abstract fun bind(data: Data)
}

class EmptyViewHolder<T>(itemView: View) : ViewHolder<T>(itemView) {

    override fun bind(data: T) {
        // no op
    }
}
