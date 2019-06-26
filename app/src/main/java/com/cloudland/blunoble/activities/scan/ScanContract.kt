package com.cloudland.blunoble.activities.scan

import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.ScanResult
import android.content.Context

class ScanContract {

    interface ScanView {
        fun getContext(): Context

        fun processScanResult(result: ScanResult)

    }

    interface ScanPresenter {
        fun setScanning(scanning: Boolean)

        fun isScanning(): Boolean

        fun onDestroy()

        fun getBleAdapter(): BluetoothAdapter?

        fun startScan()

        fun stopScan()

    }
}