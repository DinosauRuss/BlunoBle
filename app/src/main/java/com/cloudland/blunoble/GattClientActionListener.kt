package com.cloudland.blunoble

import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context

interface GattClientActionListener {

    fun setConnected()

    fun disconnectGattServer()

    fun setSerialRxTxCharacteristic(characteristic: BluetoothGattCharacteristic?)

    fun connectionResult(result: Boolean)

    fun writeResult(result: Boolean)

}