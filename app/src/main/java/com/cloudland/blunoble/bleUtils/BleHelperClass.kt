package com.cloudland.blunoble.bleUtils

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.le.BluetoothLeScanner
import android.os.Handler

/*
Abstract class that BleScanHelper and BleConnectionHelper inherit from
 */
abstract class BleHelperClass {

    companion object {
        const val SCAN_PERIOD = 10000L
        var bleAdapter: BluetoothAdapter? = null
        var bleGatt: BluetoothGatt? = null
        var bleScanner: BluetoothLeScanner? = null
        var mCharacteristic: BluetoothGattCharacteristic? = null
    }

    val mHandler: Handler = Handler()

}