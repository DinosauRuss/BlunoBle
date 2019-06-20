package com.cloudland.blunoble.activities

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothManager
import android.content.Context
import com.cloudland.blunoble.utils.GattClientActionListener
import com.cloudland.blunoble.utils.GattClientCallback

class BleHelper(private val context: Context, address: String): GattClientActionListener {

    private val bleInteractor: BleInteractor = context as BleInteractor

    private var bleAdapter: BluetoothAdapter? = null
    private var bleGatt: BluetoothGatt? = null
    private var mCharacteristic: BluetoothGattCharacteristic? = null

    init {
        val bleManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bleAdapter = bleManager.adapter
        connectDevice(address)
    }


    private fun connectDevice(address: String) {
        if (bleInteractor.hasPermissions(bleAdapter)) {
            val device = bleAdapter?.getRemoteDevice(address)
            bleGatt = device?.connectGatt(
                context, false,
                GattClientCallback(this)
            )
        }
    }

    fun sendValueToDevice(message: String?) {
        message?.apply {
            if (mCharacteristic != null && bleGatt != null) {
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
        bleInteractor.onGattDisconnect()
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
        bleInteractor.onGattConnectionResult(result)
    }

    override fun writeSuccessOrFail(result: Boolean) {
        bleInteractor.onWriteSuccessOrFailure(result)
    }
}