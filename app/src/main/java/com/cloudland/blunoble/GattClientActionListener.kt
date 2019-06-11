package com.cloudland.blunoble

import android.bluetooth.BluetoothGattCharacteristic

interface GattClientActionListener {

    fun disconnectGattServer()

    fun setSerialRxTxCharacteristic(characteristic: BluetoothGattCharacteristic?)

    fun connectionResult(result: Boolean)

    fun writeSuccess(result: Boolean)

}