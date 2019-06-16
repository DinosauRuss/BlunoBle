package com.cloudland.blunoble.activities

import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.TextInputEditText
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import com.cloudland.blunoble.R
import com.cloudland.blunoble.utils.Utils
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsActivity : AppCompatActivity() {

    private lateinit var sharedPrefs: SharedPreferences
    private lateinit var inputs: ArrayList<TextInputEditText>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(application)
        inputs = arrayListOf(
            prefInputUp,
            prefInputDown,
            prefInputLeft,
            prefInputRight,
            prefInputY,
            prefInputX,
            prefInputB,
            prefInputA)

        fillTextInputLayouts()
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
        btnSaveSettingsActivity.setOnClickListener { saveCommandsToSharedPrefs() }
        btnCancelSettingsActivity.setOnClickListener { onBackPressed() }
    }

    override fun onBackPressed() {
        Toast.makeText(this, getString(R.string.toast_not_saved), Toast.LENGTH_SHORT).show()
        super.onBackPressed()
    }

    private fun saveCommandsToSharedPrefs() {
        val edito = sharedPrefs.edit()
        val button_pref_keys = resources.obtainTypedArray(R.array.BUTTON_PREF_KEYS)
        val button_pref_defaults = resources.obtainTypedArray(R.array.BUTTON_PREF_DEFAULTS)

        inputs.forEachIndexed {index, input ->
            val inputCommand = input.text.toString()
            val saveCommand: String
            saveCommand = if (inputCommand.isNotEmpty()) {
                inputCommand
            } else {
                button_pref_defaults.getString(index) ?: "0"
            }
            edito.putString(button_pref_keys.getString(index), saveCommand)
        }
        edito.apply()
        button_pref_keys.recycle()
        button_pref_defaults.recycle()

        Toast.makeText(this, getString(R.string.toast_saved), Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun fillTextInputLayouts() {
        val button_pref_keys = resources.obtainTypedArray(R.array.BUTTON_PREF_KEYS)
        val button_pref_defaults = resources.obtainTypedArray(R.array.BUTTON_PREF_DEFAULTS)

        inputs.forEachIndexed() { index, input ->
            val key = button_pref_keys.getString(index)
            val default = button_pref_defaults.getString(index)
            val valFromPref = sharedPrefs.getString(key, default) ?: default
            input.setText(valFromPref)
        }
    }

}
