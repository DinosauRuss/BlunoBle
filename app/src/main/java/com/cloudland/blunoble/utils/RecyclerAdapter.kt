package com.cloudland.blunoble.utils

import android.support.graphics.drawable.AnimatedVectorDrawableCompat
import android.support.v7.recyclerview.extensions.ListAdapter
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cloudland.blunoble.R
import kotlinx.android.synthetic.main.list_item_recycler.view.*

class RecyclerAdapter(
    private val listener: RecyclerInteractionListener?):
        ListAdapter<String, RecyclerAdapter.ViewHolder>(DIFF_CALLBACK) {


    companion object {
        private val DIFF_CALLBACK = object: DiffUtil.ItemCallback<String>() {
            override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
                return oldItem == newItem
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_recycler, parent, false)

        return ViewHolder(view, listener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindData( getItem(position) )
    }

    fun updateList(list: ArrayList<String>) {
        // Create a temporary local scope list because submitList
        // will not work if submitting the list it already has
        val tempList = ArrayList<String>()
        tempList.addAll(list)
        submitList(tempList)
    }


    class ViewHolder(itemView: View, private val listener: RecyclerInteractionListener?)
        : RecyclerView.ViewHolder(itemView) {

        fun bindData(command: String) {
            itemView.tvItemRecycler.text = command
            itemView.setOnLongClickListener {
                listener?.onItemLongPress( adapterPosition )
                return@setOnLongClickListener true
            }
            itemView.btnSendRecycler.setOnClickListener {
                listener?.onSendImagePress(command)
                val drawable = it.btnSendRecycler.drawable
                if (drawable is AnimatedVectorDrawableCompat) {
                    drawable.start()
                }
            }
        }
    }

    interface RecyclerInteractionListener {
        fun onItemLongPress(position: Int)
        fun onSendImagePress(command: String)
    }

}