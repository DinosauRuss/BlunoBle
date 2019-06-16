package com.cloudland.blunoble.utils

import android.content.Context
import android.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SharedPrefObject(context: Context) {

    private val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)

    fun insertSinglecommand(key: String, value: String) {
        val edito = sharedPrefs.edit()
        edito.putString(key, value)
        edito.apply()
    }

    fun retrieveSingleCommand(key: String?, defaultValue: String): String {
        return sharedPrefs.getString(key, defaultValue) ?: defaultValue
    }

    fun insertList(key: String, list: ArrayList<String>) {
        val edito = sharedPrefs.edit()
        val jsonList = Gson().toJson(list)
        edito.putString(key, jsonList)
        edito.apply()
    }

    fun retrieveList(key: String): ArrayList<String> {
        val jsonString = sharedPrefs.getString(key, "") ?: ""
        val tokenType = object : TypeToken<ArrayList<String>>() {}.type
        return Gson().fromJson(jsonString, tokenType) ?: ArrayList()
    }
}