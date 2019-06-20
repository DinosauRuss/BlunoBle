package com.cloudland.blunoble.bleUtils

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.ParcelUuid
import android.util.Log
import com.cloudland.blunoble.utils.Utils

/*
Class to separate Ble scanning logic
 */
class BleScanHelper(private val bleScanInteractor: BleInteractor.Scanner): BleHelperClass() {

    init {
        val bleManager = bleScanInteractor.getContext().getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bleAdapter = bleManager.adapter
        bleScanner = bleAdapter?.bluetoothLeScanner
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
            bleScanInteractor.stopScan()
        }, SCAN_PERIOD)
    }

    fun stopScan() {
        bleScanner?.stopScan(leScanCallback)
        mHandler.removeCallbacksAndMessages(null)
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
            bleScanInteractor.processScanResult(result)
        }
    }
}