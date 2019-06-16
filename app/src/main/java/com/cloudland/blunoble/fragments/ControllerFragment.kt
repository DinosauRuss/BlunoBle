package com.cloudland.blunoble.fragments

import android.content.Context
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton

import com.cloudland.blunoble.R
import com.cloudland.blunoble.utils.Utils
import kotlinx.android.synthetic.main.fragment_controller.*

private const val PARAM_NAME = "NAME"
private const val PARAM_ADDRESS = "address"

class ControllerFragment : Fragment() {

    companion object {
        @JvmStatic
        fun newInstance(name: String, address: String) =
            ControllerFragment().apply {
                arguments = Bundle().apply {
                    putString(PARAM_NAME, name)
                    putString(PARAM_ADDRESS, address)
                }
            }
    }

    private var deviceName: String? = null
    private var deviceAddr: String? = null
    private var listener: OnFragmentInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            deviceName = it.getString(PARAM_NAME)
            deviceAddr = it.getString(PARAM_ADDRESS)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_controller, container, false)
    }

    override fun onResume() {
        setButtonClickListeners()
        super.onResume()
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

    private fun setButtonClickListeners() {
        val sharedprefs = PreferenceManager.getDefaultSharedPreferences(activity)
        val PREF_KEYS = resources.obtainTypedArray(R.array.BUTTON_PREF_KEYS)
        val PREF_DEFAULTS = resources.obtainTypedArray(R.array.BUTTON_PREF_DEFAULTS)
        val buttons = arrayListOf<ImageButton>(
            btnArrowUp,
            btnArrowDown,
            btnArrowLeft,
            btnArrowRight,
            btnYController,
            btnXController,
            btnBController,
            btnAController)

        btnSettingsFragController.setOnClickListener { listener?.openSettings() }
        buttons.forEachIndexed {index, imageButton ->
            val key = PREF_KEYS.getString(index)
            val default = PREF_DEFAULTS.getString(index)
            val command = sharedprefs.getString(key, default) ?: "0"
            imageButton.setOnClickListener {
                listener?.sendCommand(command)
            }
        }

        PREF_KEYS.recycle()
        PREF_DEFAULTS.recycle()
    }

}
