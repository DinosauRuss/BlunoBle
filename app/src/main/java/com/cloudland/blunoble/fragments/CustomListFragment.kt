package com.cloudland.blunoble.fragments


import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast

import com.cloudland.blunoble.R
import com.cloudland.blunoble.utils.RecyclerAdapter
import com.cloudland.blunoble.utils.SharedPrefObject
import com.cloudland.blunoble.utils.Utils
import kotlinx.android.synthetic.main.alert_dialog_input.view.*
import kotlinx.android.synthetic.main.fragment_custom_list.*

/*
A fragment for creating a list of customized and persisted commands
 */
class CustomListFragment : Fragment(), RecyclerAdapter.RecyclerInteractionListener {

    private var sharedPrefObject: SharedPrefObject? = null
    private var listener: OnFragmentInteractionListener? = null
    private val commandList = ArrayList<String>()
    private lateinit var adapto: RecyclerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_custom_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initRecyclerView()
        tvAddFragList.setOnClickListener {
            activity?.apply { createInputDialog(it.context) }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        sharedPrefObject = SharedPrefObject(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()

        sharedPrefObject = null
        listener = null
    }

    private fun initRecyclerView() {
        adapto = RecyclerAdapter(this)

        rvFragList.apply {
            this.setHasFixedSize(true)
            this.adapter = adapto
//            this.layoutManager = LinearLayoutManager(activity)
            if (activity?.resources?.configuration?.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                this.layoutManager = GridLayoutManager(activity, 2)
            } else {
                this.layoutManager = LinearLayoutManager(activity)
            }
        }

        // Load previous list of commands from SharedPreferences
        val key = resources.getString(R.string.PREF_JSON_KEY)
        val previousCommandList = sharedPrefObject?.retrieveList(key)
        previousCommandList?.apply {
            commandList.clear()
            this.forEach {
                commandList.add(it)
            }
            adapto.updateList(commandList)
        }
    }

    private fun createInputDialog(context: Context) {
        val view = LayoutInflater.from(context).inflate(R.layout.alert_dialog_input, view as ViewGroup, false)

        AlertDialog.Builder(context, R.style.MyDialogStyle)
            .setView(view)
            .setPositiveButton(getString(R.string.alert_btn_ok)) { dialog, which ->
                view.edtAlertCommand.text?.takeIf { it.isNotEmpty() }?.apply {
                    addCommandToList(this.toString())
                }
                Utils.closeSoftKeyboard(this.activity as AppCompatActivity)
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.alert_btn_cancel)) { dialog, which ->
                Utils.closeSoftKeyboard(this.activity as AppCompatActivity)
                dialog.dismiss()
            }
            .show()
    }

    private fun addCommandToList(command: String) {
        if (command !in commandList) {
            commandList.add(command)
            notifyAdapterAndSave()
        } else {
            Toast.makeText(activity, getString(R.string.toast_repeated_command), Toast.LENGTH_SHORT).show()
        }
    }

    private fun removeCommandFromList(context: Context, position: Int) {
        AlertDialog.Builder(context, R.style.MyDialogStyle)
            .setTitle("Delete Item?")
            .setNegativeButton(getString(R.string.alert_btn_cancel)) { dialog, which ->
                dialog.dismiss()
            }
            .setPositiveButton(getString(R.string.alert_btn_ok)) { dialog, which ->
                commandList.removeAt(position)
                notifyAdapterAndSave()
            }
            .show()
    }

    private fun notifyAdapterAndSave() {
        adapto.updateList(commandList)

        val key = resources.getString(R.string.PREF_JSON_KEY)
        sharedPrefObject?.insertList(key, commandList)
    }

    // ----- RecyclerInteractionListener methods -----
    override fun onItemLongPress(context: Context, position: Int) {
        removeCommandFromList(context, position)
    }

    override fun onSendImagePress(command: String) {
        listener?.sendCommand(command)
    }

}
