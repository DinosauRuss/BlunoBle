package com.cloudland.blunoble.bleUtils

import android.bluetooth.*
import android.util.Log
import com.cloudland.blunoble.utils.Utils
import java.util.*

/*
Class which receives information from Ble device and executes callbacks
 */
class GattClientCallback( private val mClientActionListener: GattClientActionListener) : BluetoothGattCallback() {

    override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
        super.onConnectionStateChange(gatt, status, newState)

        Log.d(Utils.TAG, "connection state change: $newState")

        if (status != BluetoothGatt.GATT_SUCCESS) {
            // handle anything not SUCCESS as failure
            mClientActionListener.connectionResult(false)
            mClientActionListener.disconnectGattServer()
            return
        }

        if (newState == BluetoothProfile.STATE_CONNECTED) {
            gatt.discoverServices()
        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
            mClientActionListener.disconnectGattServer()
        }
    }

    override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
        super.onServicesDiscovered(gatt, status)

        if (status != BluetoothGatt.GATT_SUCCESS) {
            Log.d(Utils.TAG, "Connect fail")
            mClientActionListener.disconnectGattServer()
            mClientActionListener.connectionResult(false)
            return
        }

        Log.d(Utils.TAG, "Connect success, services discovered")

        val service: BluetoothGattService? = gatt.getService(UUID.fromString(Utils.SERVICE_SERIAL_UUID))
        val rxTxCharacteristic =
            service?.getCharacteristic(UUID.fromString(Utils.CHARACTERISTIC_SERIAL_RXTX_UUID))
        rxTxCharacteristic?.also {
            mClientActionListener.setSerialRxTxCharacteristic(it)
            mClientActionListener.connectionResult(true)
        }
    }

    override fun onCharacteristicWrite(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
        super.onCharacteristicWrite(gatt, characteristic, status)
        Log.d(Utils.TAG, "onCharacteristicWrite")
        if (status == BluetoothGatt.GATT_SUCCESS) {
            Log.d(Utils.TAG, "characteristic write success")
//            mClientActionListener.writeSuccessOrFail(true)
        } else {
            Log.d(Utils.TAG, "characteristic write failure")
            mClientActionListener.writeSuccessOrFail(false)
            mClientActionListener.disconnectGattServer()
        }
    }

}