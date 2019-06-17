package com.cloudland.blunoble.activities

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.View
import android.widget.Toast
import com.cloudland.blunoble.R
import com.cloudland.blunoble.fragments.OnFragmentInteractionListener
import com.cloudland.blunoble.utils.GattClientActionListener
import com.cloudland.blunoble.utils.GattClientCallback
import com.cloudland.blunoble.utils.Utils
import com.cloudland.blunoble.utils.ViewPagerAdapter
import kotlinx.android.synthetic.main.activity_pager.*
import kotlinx.android.synthetic.main.fragment_terminal.*

class PagerActivity : AppCompatActivity(), GattClientActionListener, OnFragmentInteractionListener {

    companion object {
        const val INTENT_EXTRAS_NAME = "name"
        const val INTENT_EXTRAS_ADDRESS = "address"

        private var bleAdapter: BluetoothAdapter? = null
        private var bleScanner: BluetoothLeScanner? = null
        private var bleGatt: BluetoothGatt? = null
        private var mCharacteristic: BluetoothGattCharacteristic? = null

        private var mConnected = false

        private var DEVICE_ADDR: String? = null
    }

    private val REQUEST_ENABLE_BT = 1
    private val REQUEST_FINE_LOCATION = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pager)

        val intenta = intent
        val deviceName = intenta.getStringExtra(INTENT_EXTRAS_NAME)
        val deviceAddress = intenta.getStringExtra(INTENT_EXTRAS_ADDRESS)
        DEVICE_ADDR = deviceAddress

        if (!mConnected) {
            val bleManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            bleAdapter = bleManager.adapter
            bleScanner = bleAdapter?.bluetoothLeScanner
            connectDevice(deviceAddress)

            setLoadingVisibility()
        }

        val vpAdapter = ViewPagerAdapter(deviceName, deviceAddress, supportFragmentManager)
        viewPager.adapter = vpAdapter
        tabLayout.setupWithViewPager(viewPager)

        tvNamePagerActivity.text = deviceName
        tvAddressPagerActivity.text = deviceAddress
        btnDisconnectPagerActivity.setOnClickListener { unlinkBleDevice() }
    }

    override fun onResume() {
        super.onResume()

        // Verify phone is BLE capable
        // Is also set in the manifest
        packageManager.takeIf { !packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE) }?.also {
            Toast.makeText(this, getString(R.string.toast_no_ble), Toast.LENGTH_SHORT).show()
            finish()
        }

        if (!hasPermissions()) {
            Toast.makeText(this, getString(R.string.toast_incorrect_permissions), Toast.LENGTH_SHORT).show()
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
                .setTitle( "${getString(R.string.alert_disconnect_title)}!" )
                .setMessage(getString(R.string.alert_disconnect_message).format(bleGatt?.device?.name))
                .setNegativeButton(getString(R.string.alert_btn_no)) { dialog, id ->
                    dialog.dismiss()
                }
                .setPositiveButton(getString(R.string.alert_btn_yes)) { dialog, id ->
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

    private fun requestBleEnable() {
        val enableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        startActivityForResult(enableIntent, REQUEST_ENABLE_BT)
    }

    private fun connectDevice(address: String) {
        if (hasPermissions()) {
            val device = bleAdapter?.getRemoteDevice(address)
            bleGatt = device?.connectGatt(this, false,
                GattClientCallback(this)
            )
        }
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

    private fun sendValueToDevice(message: String?) {
        message?.apply {
            if (mCharacteristic != null && bleGatt != null && mConnected) {
                mCharacteristic!!.setValue(this)
                bleGatt!!.writeCharacteristic(mCharacteristic)
            }
        }
    }

    private fun setLoadingVisibility() {
        viewPager.visibility = View.INVISIBLE
        tabLayout.visibility = View.INVISIBLE
        progressBarPagerActivity.visibility = View.VISIBLE
    }

    private fun setConnectedVisibility() {
        viewPager.visibility = View.VISIBLE
        tabLayout.visibility = View.VISIBLE
        progressBarPagerActivity.visibility = View.INVISIBLE
    }


    // ----- Gatt action Listener -----

    override fun disconnectGattServer() {
        Log.d(Utils.TAG, "pager disconnectGatt")
        mConnected = false
        Toast.makeText(this, getString(R.string.toast_device_disconnected), Toast.LENGTH_SHORT).show()
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
                mConnected = true
                this.runOnUiThread {
                    setConnectedVisibility()
                    Toast.makeText(
                        this,
                        getString(R.string.toast_connected_to).format(bleGatt?.device?.name),
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
                        getString(R.string.toast_connect_fail),
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    finish()
                }

            }
        }
    }

    override fun writeSuccess(result: Boolean) {
        when (result) {
//            true -> {
//                runOnUiThread {
//                    Toast.makeText(
//                        this,
//                        getString(R.string.toast_send_data).format(getString(R.string.toast_success)),
//                        Toast.LENGTH_SHORT
//                    )
//                        .show()
//                }
//            }
            false -> {
                runOnUiThread {
                    Toast.makeText(
                        this,
                        getString(R.string.toast_send_data).format(getString(R.string.toast_failure)),
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            }
        }
    }

    // ----- Fragment interaction methods -----

    override fun unlinkBleDevice() {
        onBackPressed()
    }

    override fun sendCommand(command: String?) {
        sendValueToDevice(command)
    }

    override fun checkConnected(): Boolean {
        return mConnected
    }

    override fun openSettings() {
        val intento = Intent(this, SettingsActivity::class.java)
        startActivity(intento)
    }

}
