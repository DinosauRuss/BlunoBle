package com.cloudland.blunoble.fragments

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_terminal.*
import android.app.Activity
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.inputmethod.InputMethodManager
import com.cloudland.blunoble.utils.Utils

/*
Fragment to input one-time commands
 */
class TerminalFragment : Fragment() {

    private var listener: OnFragmentInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(com.cloudland.blunoble.R.layout.fragment_terminal, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnSendFragCommand.setOnClickListener {
            //            listener?.sendCommand(getTextFromEdt())
            getTextFromEdt().takeIf { it != null }?.also { listener?.sendCommand(it) }
            Utils.closeSoftKeyboard(this.activity as AppCompatActivity)

        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)

        if (!isVisibleToUser) {
            activity?.apply { Utils.closeSoftKeyboard(this as AppCompatActivity) }
        }
    }

    private fun getTextFromEdt(): String? {
        val msg = if (edtMsgFragCommand.text.isNotEmpty()) "${edtMsgFragCommand.text}" else null
        msg?.apply { edtMsgFragCommand.setText("") }

        return msg
    }

}
