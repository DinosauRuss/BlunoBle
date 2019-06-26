package com.cloudland.blunoble.activities.scan

import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.ScanResult
import android.content.Context
import com.cloudland.blunoble.bleUtils.BleInteractor
import com.cloudland.blunoble.bleUtils.BleScanHelper

class ScanPresenter(private var scanView: ScanContract.ScanView?) :
    ScanContract.ScanPresenter,
    BleInteractor.Scanner {

    private var mScanning: Boolean = false
    private var bleScanHelper: BleScanHelper = BleScanHelper(this)

    override fun isScanning(): Boolean {
        return mScanning
    }

    override fun onDestroy() {
        scanView = null
    }

    override fun getBleAdapter(): BluetoothAdapter? {
        return bleScanHelper.getBleAdapter()
    }

    override fun startScan() {
        bleScanHelper.startScan()
        mScanning = true
    }

    // BleScanner.Interactor methods
    override fun stopScan() {
        mScanning = false
        bleScanHelper.stopScan()
        scanView?.onStopScanCallback()
    }

    override fun getContext(): Context? {
        return scanView?.getContext()
    }

    override fun processScanResult(result: ScanResult) {
        scanView?.processScanResult(result)
    }
}