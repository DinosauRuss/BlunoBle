package com.cloudland.blunoble.utils

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothManager
import android.bluetooth.le.*
import android.content.Context
import android.os.Handler
import android.os.ParcelUuid
import android.util.Log

class BleHelper(private val bleInteractor: BleInteractor, connected: Boolean, address: String? = null)
    : GattClientActionListener {

    companion object {
        private const val SCAN_PERIOD = 10000L
        private var bleAdapter: BluetoothAdapter? = null
        private var bleGatt: BluetoothGatt? = null
        private var bleScanner: BluetoothLeScanner? = null
        private var mCharacteristic: BluetoothGattCharacteristic? = null
    }

    private val mHandler: Handler = Handler()

    init {
        val bleManager = bleInteractor.getContext().getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bleAdapter = bleManager.adapter
        bleScanner = bleAdapter?.bluetoothLeScanner
        if (!connected) {
            address?.also { connectDevice(it) }
        }
    }

    fun startScan() {
        val uuidFilter = ScanFilter.Builder()
            .setServiceUuid(ParcelUuid.fromString(Utils.SERVICE_SERIAL_UUID))
            .build()
        val filterList = ArrayList<ScanFilter>()
        filterList.add(uuidFilter)

        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
            .build()

        bleScanner?.startScan(filterList, settings, leScanCallback)

        // Stops scanning after a period
        mHandler.postDelayed({
            bleInteractor.stopScan()
        }, SCAN_PERIOD)
    }

    fun stopScan() {
        bleScanner?.stopScan(leScanCallback)
        mHandler.removeCallbacksAndMessages(null)
    }

    private fun connectDevice(address: String) {
        if (bleInteractor.hasPermissions(bleAdapter)) {
            val device = bleAdapter?.getRemoteDevice(address)
            bleGatt = device?.connectGatt(
                bleInteractor.getContext(),
                false,
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

    private val leScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            Log.d(Utils.TAG, "${result?.device}, ${result?.device?.name}")
            result?.apply { processResult(this) }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e(Utils.TAG, "Scan failed: $errorCode")
        }

        private fun processResult(result: ScanResult) {
            bleInteractor.processScanResult(result)
        }
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