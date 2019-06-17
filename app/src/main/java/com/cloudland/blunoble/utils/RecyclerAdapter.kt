package com.cloudland.blunoble.utils

import android.support.graphics.drawable.AnimatedVectorDrawableCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cloudland.blunoble.R
import com.cloudland.blunoble.fragments.OnFragmentInteractionListener
import kotlinx.android.synthetic.main.list_item_recycler.view.*

class RecyclerAdapter(
    private var commandList: ArrayList<String>,
    private val listener: RecyclerInteractionListener?):
        RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_recycler, parent, false)

        return ViewHolder(view, listener)
    }

    override fun getItemCount(): Int {
        return commandList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindData(commandList[position], position)
    }

    fun notifyListChanged(list: ArrayList<String>) {
        commandList = list
        notifyDataSetChanged()
    }


    class ViewHolder(itemView: View, private val listener: RecyclerInteractionListener?)
        : RecyclerView.ViewHolder(itemView) {

        fun bindData(command: String, position: Int) {
            itemView.tvItemRecycler.text = command
            itemView.setOnLongClickListener {
                listener?.onItemLongPress(position)
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