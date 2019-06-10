package com.cloudland.blunoble

import android.Manifest
import android.app.Activity
import android.bluetooth.*
import android.bluetooth.le.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), GattClientActionListener {

    companion object {
        val INTENT_EXTRAS_NAME = "name"
        val INTENT_EXTRAS_ADDRESS = "address"
    }

    private val REQUEST_ENABLE_BT = 1
    private val REQUEST_FINE_LOCATION = 2

    private var bleAdapter: BluetoothAdapter? = null
    private var bleScanner: BluetoothLeScanner? = null
    private var bleGatt: BluetoothGatt? = null
    private var mCharacteristic: BluetoothGattCharacteristic? = null

    private var mConnected = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val intenta = intent
        val name = intenta.getStringExtra(INTENT_EXTRAS_NAME)
        val deviceAddress = intenta.getStringExtra(INTENT_EXTRAS_ADDRESS)
        tvMainName.text = name
        tvMainAddress.text = deviceAddress

        val bleManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bleAdapter = bleManager.adapter
        bleScanner = bleAdapter?.bluetoothLeScanner
        connectDevice(deviceAddress)

        btnSend.setOnClickListener { sendValue() }
        btnDisconnect.setOnClickListener { onBackPressed() }
    }

    override fun onResume() {
        super.onResume()

        // Verify phone is BLE capable
        // Also set in the manifest
        packageManager.takeIf { !packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE) }?.also {
            Toast.makeText(this, "No bluetooth LE", Toast.LENGTH_SHORT).show()
            finish()
        }

        if (!hasPermissions()) {
            Toast.makeText(this, "Incorrect permissions", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish()
            return
        }
    }

    override fun onBackPressed() {
        disconnectGattServer()
        super.onBackPressed()
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

    private fun connectDevice(address: String) {
        if (hasPermissions()) {
            val device = bleAdapter?.getRemoteDevice(address)
            bleGatt = device?.connectGatt(this, false, GattClientCallback(this))
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
                mConnected = true
                this.runOnUiThread {
                    progressMain.visibility = View.GONE
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
                    onBackPressed()
                }

            }
        }
    }

    override fun writeSuccess(result: Boolean) {
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
