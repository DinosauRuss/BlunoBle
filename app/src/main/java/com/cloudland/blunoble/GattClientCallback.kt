package com.cloudland.blunoble

import android.bluetooth.*
import android.util.Log

class GattClientCallback(private val mClientActionListener: GattClientActionListener): BluetoothGattCallback() {

    override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
        super.onConnectionStateChange(gatt, status, newState)

        if (status == BluetoothGatt.GATT_FAILURE) {
            mClientActionListener.disconnectGattServer()
            return
        } else if (status != BluetoothGatt.GATT_SUCCESS) {
            // handle anything not SUCCESS as failure
            mClientActionListener.disconnectGattServer()
            return
        }

        if (newState == BluetoothProfile.STATE_CONNECTED) {
            Log.d(Utils.TAG, "gatt: $gatt")
            gatt.discoverServices()
            mClientActionListener.setConnected()
        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
            mClientActionListener.disconnectGattServer()
        }
    }

    override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
        super.onServicesDiscovered(gatt, status)

        if (status != BluetoothGatt.GATT_SUCCESS) {
            Log.d(Utils.TAG, "connect fail")
            return
        }

        Log.d(Utils.TAG, "connect success, services discovered")

//        val chars = ArrayList<BluetoothGattCharacteristic>()
        for (service: BluetoothGattService in gatt.services) {
            Log.d(Utils.TAG, "\nservice: ${service.uuid}")

            for (characteristic: BluetoothGattCharacteristic in service.characteristics) {
                Log.d(Utils.TAG, "char: ${characteristic.uuid}")
            }
        }
    }

    override fun onCharacteristicWrite(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
        super.onCharacteristicWrite(gatt, characteristic, status)
        if (status == BluetoothGatt.GATT_SUCCESS) {
            Log.d(Utils.TAG, "characteristic write success")


        } else {
            Log.d(Utils.TAG, "characteristic write failure")
            mClientActionListener.disconnectGattServer()
        }
    }

    override fun onDescriptorWrite(gatt: BluetoothGatt, descriptor: BluetoothGattDescriptor, status: Int) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            Log.d(Utils.TAG, "descriptor write success")
        } else {
            Log.d(Utils.TAG, "descriptor write failure")
        }
    }

}