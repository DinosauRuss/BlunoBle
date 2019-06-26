package com.cloudland.blunoble.activities.settings

import com.cloudland.blunoble.utils.SharedPrefObject

class SettingsPresenter(private var settingsView: SettingsContract.SettingsView?) :
    SettingsContract.SettingsPresenter {

    private var sharedPrefObject: SharedPrefObject = SharedPrefObject(settingsView?.getContext())


    override fun onDestroy() {
        settingsView = null
    }

    override fun getSinglePref(key: String, default: String): String {
        return sharedPrefObject.retrieveSingleCommand(key, default)
    }

    override fun saveSinglePref(key: String, value: String) {
        sharedPrefObject.insertSinglecommand(key, value)
    }

}