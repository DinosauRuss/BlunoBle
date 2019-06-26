package com.cloudland.blunoble.activities.settings

import android.content.Context

class SettingsContract {

    interface SettingsView {
        fun getContext(): Context?
    }

    interface SettingsPresenter {
        fun onDestroy()

        fun getSinglePref(key: String, default: String): String

        fun saveSinglePref(key: String, value: String)

    }
}