package com.cloudland.blunoble.bleUtils

import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.ScanResult
import android.content.Context

abstract class BleInteractor {

    interface Scanner {
//        fun hasPermissions(bleAdapter: BluetoothAdapter?): Boolean
        fun getContext(): Context?
        fun processScanResult(result: ScanResult)
        fun stopScan()
    }

    interface Connector {
        fun hasPermissions(bleAdapter: BluetoothAdapter?): Boolean?
        fun getContext(): Context?
        fun onGattDisconnect()
        fun onGattConnectionResult(connect: Boolean)
        fun onWriteSuccessOrFailure(result: Boolean)
    }

}