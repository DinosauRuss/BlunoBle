package com.cloudland.blunoble.bleUtils

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothManager
import android.content.Context
import android.util.Log
import com.cloudland.blunoble.utils.Utils

/*
Class to separate Ble connection logic
 */
class BleConnectionHelper(
    private val bleConnectInteractor: BleInteractor.Connector,
    address: String,
    connected: Boolean)
    : BleHelperClass(), GattClientActionListener {

    init {
        val bleManager = bleConnectInteractor.getContext().getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bleAdapter = bleManager.adapter
        bleScanner = bleAdapter?.bluetoothLeScanner
        if (!connected) {
            connectDevice(address)
        }
    }

    private fun connectDevice(address: String) {
        if (bleConnectInteractor.hasPermissions(bleAdapter)) {
            val device = bleAdapter?.getRemoteDevice(address)
            bleGatt = device?.connectGatt(
                bleConnectInteractor.getContext(),
                false,
                GattClientCallback(this)
            )
        }
    }

    fun sendValueToDevice(message: String?) {
        message?.apply {
            if (mCharacteristic != null && bleGatt != null) {
                Log.d(Utils.TAG, "inside if")
                mCharacteristic!!.setValue(this)
                bleGatt!!.writeCharacteristic(mCharacteristic)
            }
        }
    }

    fun getBleAdapter(): BluetoothAdapter? {
        return bleAdapter
    }

    // ----- Gatt action Listener -----
    override fun disconnectGattServer() {
        bleConnectInteractor.onGattDisconnect()
        bleGatt?.apply {
            this.disconnect()
            this.close()
            bleGatt = null
            mCharacteristic = null
        }
    }

    override fun setSerialRxTxCharacteristic(characteristic: BluetoothGattCharacteristic?) {
        mCharacteristic = characteristic
    }

    override fun connectionResult(result: Boolean) {
        bleConnectInteractor.onGattConnectionResult(result)
    }

    override fun writeSuccessOrFail(result: Boolean) {
        bleConnectInteractor.onWriteSuccessOrFailure(result)
    }

}