package com.cloudland.blunoble.activities.settings

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.TextInputEditText
import android.view.WindowManager
import android.widget.Toast
import com.cloudland.blunoble.R
import com.cloudland.blunoble.utils.Utils
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsActivity : AppCompatActivity(), SettingsContract.SettingsView {

    private var mSettingsPresenter: SettingsContract.SettingsPresenter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Text inputs from xml
        val inputs = arrayListOf(
            prefInputUp,
            prefInputDown,
            prefInputLeft,
            prefInputRight,
            prefInputA,
            prefInputB,
            prefInputX,
            prefInputY
            )

        mSettingsPresenter = SettingsPresenter(this)
        fillTextInputLayouts(inputs)

        btnSaveSettingsActivity.setOnClickListener { saveTextInputsToSharedPrefs(inputs) }
        btnCancelSettingsActivity.setOnClickListener { onBackPressed() }

        // Prevent keyboard from opening automatically
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)

        supportActionBar?.hide()
    }

    override fun onDestroy() {
        Utils.closeSoftKeyboard(this)
        mSettingsPresenter?.onDestroy()

        super.onDestroy()
    }

    override fun onBackPressed() {
        Toast.makeText(this, getString(R.string.toast_not_saved), Toast.LENGTH_SHORT).show()
        super.onBackPressed()
    }

    override fun getContext(): Context? {
        return this.applicationContext
    }

    private fun saveTextInputsToSharedPrefs(textInputs: ArrayList<TextInputEditText>) {
        val button_pref_keys = resources.obtainTypedArray(R.array.BUTTON_PREF_KEYS)
        val button_pref_defaults = resources.obtainTypedArray(R.array.BUTTON_PREF_DEFAULTS)

        textInputs.forEachIndexed {index, input ->
            val inputCommand = input.text.toString()
            val saveCommand = if (inputCommand.isNotEmpty()) {
                inputCommand
            } else {
                button_pref_defaults.getString(index) ?: "0"
            }
            val key = button_pref_keys.getString(index)
            if (key != null) {
                mSettingsPresenter?.saveSinglePref(key, saveCommand)
            }
        }
        button_pref_keys.recycle()
        button_pref_defaults.recycle()

        Toast.makeText(this, getString(R.string.toast_saved), Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun fillTextInputLayouts(textInputs: ArrayList<TextInputEditText>) {
        val button_pref_keys = resources.obtainTypedArray(R.array.BUTTON_PREF_KEYS)
        val button_pref_defaults = resources.obtainTypedArray(R.array.BUTTON_PREF_DEFAULTS)

        textInputs.forEachIndexed { index, input ->
            val key = button_pref_keys.getString(index)
            val default = button_pref_defaults.getString(index) ?: "0"
            if (key != null) {
                val valFromPref = mSettingsPresenter?.getSinglePref(key, default)
                input.setText(valFromPref)
            }
        }

        button_pref_keys.recycle()
        button_pref_defaults.recycle()
    }

}
