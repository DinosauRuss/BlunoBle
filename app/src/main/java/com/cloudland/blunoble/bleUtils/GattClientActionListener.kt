package com.cloudland.blunoble.bleUtils

import android.bluetooth.BluetoothGattCharacteristic

/*
Interface to receive callbacks from a BluetoothGattCallback
 */
interface GattClientActionListener {

    fun disconnectGattServer()

    fun setSerialRxTxCharacteristic(characteristic: BluetoothGattCharacteristic?)

    fun connectionResult(result: Boolean)

    fun writeSuccessOrFail(result: Boolean)

}