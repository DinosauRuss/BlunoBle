package com.cloudland.blunoble

import android.Manifest
import android.app.Activity
import android.bluetooth.*
import android.bluetooth.le.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.ParcelUuid
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity(), GattClientActionListener {

    private val REQUEST_ENABLE_BT = 1
    private val REQUEST_FINE_LOCATION = 2
    private val SCAN_PERIOD = 10000L
    private var mScanning = false

    // Simple array adapter to display advertising device names
    private lateinit var listAdapter: ArrayAdapter<String>
    private val deviceArray = arrayListOf<BluetoothDevice>()

    private var bleAdapter: BluetoothAdapter? = null
    private var bleScanner: BluetoothLeScanner? = null
    private var bleGatt: BluetoothGatt? = null
    private var mCharacteristic: BluetoothGattCharacteristic? = null

    private var mConnected = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bleManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bleAdapter = bleManager.adapter
        bleScanner = bleAdapter?.bluetoothLeScanner

        listAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1)
        lvMain.adapter = listAdapter

        btnStartScan.setOnClickListener { startScan() }
        btnStopScan.setOnClickListener { stopScan() }
        btnStopScan.isEnabled = false
        btnConnect.isEnabled = false
//        btnDisconnect.isEnabled = false
        btnDisconnect.setOnClickListener { disconnectGattServer() }

        btnSend.setOnClickListener { sendValue() }
    }

    override fun onResume() {
        super.onResume()

        // Verify phone is BLE capable
        // Also set in the manifest
        packageManager.takeIf { !packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE) }?.also {
            Toast.makeText(this, "No bluetooth LE", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onPause() {
        super.onPause()

        stopScan()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish()
            return
        } else if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_OK) {
            recreate()
        }
    }

    private fun startScan() {
        if (mScanning || !hasPermissions()) {
            Log.d(Utils.TAG, "Cannot start scan")
            return
        }

        val uuidFilter = ScanFilter.Builder()
            .setServiceUuid(ParcelUuid.fromString(Utils.SERIAL_SERVICE_UUID))
            .build()
        val filterList = ArrayList<ScanFilter>()
        filterList.add(uuidFilter)

        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
            .build()

        bleScanner?.startScan(filterList, settings, leScanCallback)
        mScanning = true
        listAdapter.clear()
        deviceArray.clear()
        toggleButtonStates()
        btnConnect.isEnabled = false

        // Stops scanning after a period
        Handler().postDelayed({
            stopScan()
        }, SCAN_PERIOD)
    }

    private fun stopScan() {
        if (mScanning && (bleAdapter != null) && (bleAdapter?.isEnabled == true) && (bleScanner != null)) {
            mScanning = false
            bleScanner?.stopScan(leScanCallback)
            toggleButtonStates()
            scanComplete()
        }
    }

    private val leScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            Log.d(Utils.TAG, "${result?.device?.name}, ${result?.device?.uuids}")
            result?.apply { processResult(this) }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e(Utils.TAG, "Scan failed: $errorCode")
        }

        private fun processResult(result: ScanResult) {
            result.device?.apply {
                if (this !in deviceArray) {
                    deviceArray.add(result.device)
                    listAdapter.add(result.device.name)
                    listAdapter.notifyDataSetChanged()
                }
            }
        }
    }

    private fun scanComplete() {
        if (deviceArray.isEmpty()) {
            return
        }

        btnConnect.isEnabled = true
        for (device: BluetoothDevice in deviceArray) {
            btnConnect.setOnClickListener {
                connectDevice(device)
            }
        }
    }

    private fun hasPermissions(): Boolean {
        if (bleAdapter == null || bleAdapter?.isEnabled == false) {
            requestBleEnable()
            return false
        } else if (!hasLocationPermission()) {
            requestLocationPermission()
            return false
        }
        return true
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            REQUEST_FINE_LOCATION
        )
    }

    private fun requestBleEnable() {
        val enableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        startActivityForResult(enableIntent, REQUEST_ENABLE_BT)
    }

    private fun toggleButtonStates() {
        btnStartScan.isEnabled = !btnStartScan.isEnabled
        btnStopScan.isEnabled = !btnStopScan.isEnabled
    }

    private fun connectDevice(device: BluetoothDevice) {
        if (hasPermissions()) {
            bleGatt = device.connectGatt(this, false, GattClientCallback(this))
        }
    }

    private fun sendValue() {
        val txt = if (edtMsg.text.isNotEmpty()) "${edtMsg.text}\n" else null
        txt?.apply {
            if (mCharacteristic != null && bleGatt != null) {
                mCharacteristic!!.setValue(txt)
                bleGatt!!.writeCharacteristic(mCharacteristic)
            }
            edtMsg.setText("")
        }
    }


    // ----- Gatt action Listener -----

    override fun setConnected() {
        Log.d(Utils.TAG, "main setconnected $bleGatt")
        mConnected = true
        btnConnect.isEnabled = !mConnected
    }

    override fun disconnectGattServer() {
        Log.d(Utils.TAG, "main disconnectGatt")
        mConnected = false
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
        when (result) {
            true -> {
                Log.d(Utils.TAG, "result when true")
                this.runOnUiThread {
                    Toast.makeText(
                        this,
                        getString(R.string.connected_to).format(bleGatt?.device?.name),
                        Toast.LENGTH_SHORT)
                        .show()
                }
            }
            false -> {
                runOnUiThread {
                    Toast.makeText(
                        this,
                        getString(R.string.connect_fail),
                        Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    override fun writeResult(result: Boolean) {
        when (result) {
            true -> {
                runOnUiThread {
                Toast.makeText(
                    this,
                    getString(R.string.send_data).format(getString(R.string.success)),
                    Toast.LENGTH_SHORT)
                    .show()
                }
            }
            false -> {
                runOnUiThread {
                    Toast.makeText(
                        this,
                        getString(R.string.send_data).format(getString(R.string.failure)),
                        Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

}
