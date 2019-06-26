package com.cloudland.blunoble.activities.main

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.graphics.drawable.Animatable2Compat
import android.support.graphics.drawable.AnimatedVectorDrawableCompat
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.View
import android.widget.Toast
import com.cloudland.blunoble.R
import com.cloudland.blunoble.activities.settings.SettingsActivity
import com.cloudland.blunoble.fragments.OnFragmentInteractionListener
import com.cloudland.blunoble.utils.Utils
import com.cloudland.blunoble.utils.ViewPagerAdapter
import kotlinx.android.synthetic.main.activity_pager.*

class MainActivity : AppCompatActivity(),
    OnFragmentInteractionListener,
//    BleInteractor.Connector,
    MainContract.MainView {

    companion object {
        const val INTENT_EXTRAS_NAME = "name"
        const val INTENT_EXTRAS_ADDRESS = "address"

        private var mConnected = false
        private var deviceName: String? = null }

//    private var bleConnectionHelper: BleConnectionHelper? = null
    private var mMainPresenter: MainContract.MainPresenter? = null

    private val REQUEST_ENABLE_BT = 1
    private val REQUEST_FINE_LOCATION = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pager)

        val receivedIntent = intent
        deviceName = receivedIntent.getStringExtra(INTENT_EXTRAS_NAME)
        val deviceAddress = receivedIntent.getStringExtra(INTENT_EXTRAS_ADDRESS)

//        bleConnectionHelper = BleConnectionHelper(this, deviceAddress,
//            mConnected
//        )
        mMainPresenter = MainPresenter(this, deviceAddress, mConnected)

        if (!mConnected) {
            setLoadingVisibility()
        }

        val vpAdapter = ViewPagerAdapter(this, supportFragmentManager)
        viewPager.adapter = vpAdapter
        tabLayout.setupWithViewPager(viewPager)

        tvNamePagerActivity.text = deviceName
        tvAddressPagerActivity.text = deviceAddress
        btnDisconnectPagerActivity.setOnClickListener { unlinkBleDevice() }

        supportActionBar?.hide()
    }

    override fun onResume() {
        super.onResume()

        // Verify phone is BLE capable
        // Is also set in the manifest
        packageManager.takeIf { !packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE) }?.also {
            Toast.makeText(this, getString(R.string.toast_no_ble), Toast.LENGTH_SHORT).show()
            finish()
        }

//        if (!hasPermissions(bleConnectionHelper?.getBleAdapter())) {
        if (!hasPermissions(mMainPresenter?.getBleAdapter())) {
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
            AlertDialog.Builder(this, R.style.MyDialogStyle)
                .setTitle("${getString(R.string.alert_disconnect_title)}!")
                .setMessage(getString(R.string.alert_disconnect_message).format(deviceName))
                .setNegativeButton(getString(R.string.alert_btn_no)) { dialog, id ->
                    dialog.dismiss()
                }
                .setPositiveButton(getString(R.string.alert_btn_yes)) { dialog, id ->
                    dialog.dismiss()
//                    bleConnectionHelper?.disconnectGattServer()
                    mMainPresenter?.disconnect()
                    super.onBackPressed()
                }
                .show()
        }
    }

    private fun setLoadingVisibility() {
        viewPager.visibility = View.INVISIBLE
        tabLayout.visibility = View.INVISIBLE
        progressBarPagerActivity.visibility = View.VISIBLE
        val avd =
            AnimatedVectorDrawableCompat.create(this, R.drawable.avd_progress)
        avd?.registerAnimationCallback(object : Animatable2Compat.AnimationCallback() {
            override fun onAnimationEnd(drawable: Drawable?) {
                progressBarPagerActivity.post { avd.start() }
            }
        })
        progressBarPagerActivity.setImageDrawable(avd)
        avd?.start()
    }

    private fun setConnectedVisibility() {
        val drawable = progressBarPagerActivity.drawable
        if (drawable is AnimatedVectorDrawableCompat) {
            drawable.stop()
        }

        progressBarPagerActivity.visibility = View.INVISIBLE
        viewPager.visibility = View.VISIBLE
        tabLayout.visibility = View.VISIBLE
    }

    // MainContract.View methods
    override fun getContext(): Context {
        return this.applicationContext
    }

    override fun onDisconnectCallback() {
        Log.d(Utils.TAG, "pager disconnectGatt")
        if (mConnected) {
            this.runOnUiThread {
                Toast.makeText(this, getString(R.string.toast_device_disconnected), Toast.LENGTH_SHORT).show()
            }
        }
        mConnected = false
        finish()
    }

    override fun onConnectionCallback(connectSuccess: Boolean) {
        when (connectSuccess) {
            true -> {
                if (!mConnected) {
                    mConnected = true
                    this.runOnUiThread {
                        setConnectedVisibility()
                        Toast.makeText(
                            this,
                            getString(R.string.toast_connected_to).format(deviceName),
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                }
            }
            false -> {
                mConnected = false
                this.runOnUiThread {
                    Toast.makeText(
                        this,
                        getString(R.string.toast_connect_fail),
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
                finish()
            }
        }
    }

    override fun onWriteSuccess(success: Boolean) {
        if (!success) {
            this.runOnUiThread {
                Toast.makeText(this, R.string.toast_failure, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun hasPermissions(bleAdapter: BluetoothAdapter?): Boolean {
        if (bleAdapter == null || !bleAdapter.isEnabled) {
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


    // ----- Fragment interaction methods -----
    override fun unlinkBleDevice() {
        onBackPressed()
    }

    override fun sendCommand(command: String?) {
        if (mConnected) {
//            bleConnectionHelper?.sendValueToDevice(command)
            mMainPresenter?.sendValueToDevice(command)
        }
    }

    override fun checkConnected(): Boolean {
        return mConnected
    }

    override fun openSettings() {
        val intento = Intent(this, SettingsActivity::class.java)
        startActivity(intento)
    }

}
