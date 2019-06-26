package com.cloudland.blunoble.activities.main

import android.bluetooth.BluetoothAdapter
import android.content.Context

class MainContract {

    interface MainView {
        fun getContext(): Context

        fun onDisconnectCallback()

        fun onConnectionCallback(connectSuccess: Boolean)

        fun onWriteSuccess(success: Boolean)

        fun hasPermissions(bleAdapter: BluetoothAdapter?): Boolean
    }

    interface MainPresenter {
        fun onDestroy()

        fun getBleAdapter(): BluetoothAdapter?

        fun disconnect()

        fun sendValueToDevice(command: String?)

//        fun hasPermissions(bleAdapter: BluetoothAdapter?): Boolean?
    }

}