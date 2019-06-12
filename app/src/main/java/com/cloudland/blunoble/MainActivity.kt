package com.cloudland.blunoble

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
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
        const val INTENT_EXTRAS_NAME = "name"
        const val INTENT_EXTRAS_ADDRESS = "address"

        private var bleAdapter: BluetoothAdapter? = null
        private var bleScanner: BluetoothLeScanner? = null
        private var bleGatt: BluetoothGatt? = null
        private var mCharacteristic: BluetoothGattCharacteristic? = null

        private var mConnected = false
    }

    private val REQUEST_ENABLE_BT = 1
    private val REQUEST_FINE_LOCATION = 2


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val intenta = intent
        val name = intenta.getStringExtra(INTENT_EXTRAS_NAME)
        val deviceAddress = intenta.getStringExtra(INTENT_EXTRAS_ADDRESS)
        tvMainName.text = name
        tvMainAddress.text = deviceAddress

        if (!mConnected) {
            val bleManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            bleAdapter = bleManager.adapter
            bleScanner = bleAdapter?.bluetoothLeScanner
            connectDevice(deviceAddress)
        } else {
            progressMain.visibility = View.GONE
        }

        btnSend.setOnClickListener { sendValue() }
        btnDisconnect.setOnClickListener { onBackPressed() }
    }

    override fun onResume() {
        super.onResume()

        // Verify phone is BLE capable
        // Also set in the manifest
        packageManager.takeIf { !packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE) }?.also {
            Toast.makeText(this, getString(R.string.no_ble), Toast.LENGTH_SHORT).show()
            finish()
        }

        if (!hasPermissions()) {
            Toast.makeText(this, getString(R.string.incorrect_permissions), Toast.LENGTH_SHORT).show()
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
        if (mConnected) {
            val alertDialog = AlertDialog.Builder(this)
                .setTitle( "${getString(R.string.alert)}!" )
                .setMessage(getString(R.string.disconnect_from).format(bleGatt?.device?.name))
                .setNegativeButton(getString(R.string.no)) { dialog, id ->
                    dialog.dismiss()
                }
                .setPositiveButton(getString(R.string.yes)) { dialog, id ->
                    dialog.dismiss()
                    disconnectGattServer()
                    super.onBackPressed()
                }
                .show()
                val btnColor = ContextCompat.getColor(this, android.R.color.black)
                alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(btnColor)
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(btnColor)
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
                mCharacteristic!!.setValue(this)
                bleGatt!!.writeCharacteristic(mCharacteristic)
            }
            edtMsg.setText("")
        }
    }


    // ----- Gatt action Listener -----

    override fun disconnectGattServer() {
        Log.d(Utils.TAG, "main disconnectGatt")
        mConnected = false
        Toast.makeText(this, getString(R.string.device_disconnected), Toast.LENGTH_SHORT).show()
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
                Log.d(Utils.TAG, "connection true")
                mConnected = true
                this.runOnUiThread {
                    progressMain.visibility = View.GONE
                    Toast.makeText(
                        this,
                        getString(R.string.connected_to).format(bleGatt?.device?.name),
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            }
            false -> {
                mConnected = false
                runOnUiThread {
                    Toast.makeText(
                        this,
                        getString(R.string.connect_fail),
                        Toast.LENGTH_SHORT
                    )
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
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            }
            false -> {
                runOnUiThread {
                    Toast.makeText(
                        this,
                        getString(R.string.send_data).format(getString(R.string.failure)),
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            }
        }
    }

}
