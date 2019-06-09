package com.cloudland.blunoble

import android.bluetooth.*
import android.util.Log
import java.util.*

class GattClientCallback(private val mClientActionListener: GattClientActionListener) : BluetoothGattCallback() {

    override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
        super.onConnectionStateChange(gatt, status, newState)

        if (status != BluetoothGatt.GATT_SUCCESS) {
            // handle anything not SUCCESS as failure
            mClientActionListener.connectionResult(false)
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
            Log.d(Utils.TAG, "Connect fail")
            return
        }

        Log.d(Utils.TAG, "Connect success, services discovered")

        val service: BluetoothGattService? = gatt.getService(UUID.fromString(Utils.SERIAL_SERVICE_UUID))
        val rxTxCharacteristic =
                service?.getCharacteristic(UUID.fromString(Utils.SERIAL_RXTX_UUID))
        mClientActionListener.setSerialRxTxCharacteristic(rxTxCharacteristic)
        mClientActionListener.connectionResult(true)
    }

    override fun onCharacteristicWrite(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
        super.onCharacteristicWrite(gatt, characteristic, status)
        if (status == BluetoothGatt.GATT_SUCCESS) {
            Log.d(Utils.TAG, "characteristic write success")
            mClientActionListener.writeResult(true)
        } else {
            Log.d(Utils.TAG, "characteristic write failure")
            mClientActionListener.writeResult(false)
            mClientActionListener.disconnectGattServer()
        }
    }

}