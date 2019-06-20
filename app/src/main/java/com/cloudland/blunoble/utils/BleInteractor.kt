package com.cloudland.blunoble.utils

import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.ScanResult
import android.content.Context

interface BleInteractor {

    fun hasPermissions(bleAdapter: BluetoothAdapter?): Boolean
    fun getContext(): Context
    fun processScanResult(result: ScanResult)
    fun stopScan()

    fun onGattDisconnect()
    fun onGattConnectionResult(connect: Boolean)
    fun onWriteSuccessOrFailure(result: Boolean)
}