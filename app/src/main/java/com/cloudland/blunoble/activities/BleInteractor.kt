package com.cloudland.blunoble.activities

import android.bluetooth.BluetoothAdapter

interface BleInteractor {
    fun requestBleEnable()

    fun hasPermissions(bleAdapter: BluetoothAdapter?): Boolean

    fun onGattDisconnect()

    fun onGattConnectionResult(connect: Boolean)

    fun onWriteSuccessOrFailure(result: Boolean)

}