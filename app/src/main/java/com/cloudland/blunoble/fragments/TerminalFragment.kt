package com.cloudland.blunoble.fragments

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cloudland.blunoble.R
import kotlinx.android.synthetic.main.fragment_terminal.*


class TerminalFragment : Fragment() {

    private var listener: OnFragmentInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_terminal, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnSendFragCommand.setOnClickListener { listener?.sendCommand(getTextFromEdt()) }
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

    private fun getTextFromEdt(): String? {
//        val msg =  if (edtMsgFragCommand.text.isNotEmpty()) "${edtMsgFragCommand.text}\n" else null
        val msg =  if (edtMsgFragCommand.text.isNotEmpty()) "${edtMsgFragCommand.text}" else null
        msg?.apply { edtMsgFragCommand.setText("") }

        return msg
    }

}
