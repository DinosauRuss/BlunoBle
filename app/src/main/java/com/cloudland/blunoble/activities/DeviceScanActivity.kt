package com.cloudland.blunoble.activities

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.TextView
import android.widget.Toast
import com.cloudland.blunoble.R
import com.cloudland.blunoble.utils.BleHelper
import com.cloudland.blunoble.utils.BleInteractor
import com.cloudland.blunoble.utils.Utils
import kotlinx.android.synthetic.main.activity_device_scan.*

class DeviceScanActivity : AppCompatActivity(), BleInteractor {

    companion object {
        private var returnFromResult = false
    }

    private var mListAdapter: LeDeviceListAdapter? = null
    private var bleHelper: BleHelper? = null

    private val REQUEST_ENABLE_BT = 1
    private val REQUEST_FINE_LOCATION = 2
    private var mScanning = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_device_scan)

        // Verify phone is BLE capable
        // Is also set in the manifest
        packageManager.takeIf { !packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE) }?.also {
            Toast.makeText(this, getString(R.string.toast_no_ble), Toast.LENGTH_SHORT).show()
            finish()
        }

        bleHelper = BleHelper(this, false)

        if (returnFromResult) {
            startScan()
            returnFromResult = false
        }

        foundDevicesList.setOnItemClickListener { adapterView: AdapterView<*>, view1: View, position: Int, id: Long ->
            listItemClick(position) }
    }

    override fun onResume() {
        super.onResume()

        // Initialize list view adapter.
        mListAdapter = LeDeviceListAdapter()
        foundDevicesList.adapter = mListAdapter

        tvScanInstr.visibility = View.INVISIBLE
    }

    override fun onStop() {
        super.onStop()

        stopScan()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        menu?.findItem(R.id.menu_refresh)?.setActionView(R.layout.action_progress)
        if (!mScanning) {
            menu?.findItem(R.id.menu_refresh)?.isVisible = false
            menu?.findItem(R.id.menu_scan)?.isVisible = true
            menu?.findItem(R.id.menu_stop)?.isVisible = false
        } else {
            menu?.findItem(R.id.menu_refresh)?.isVisible = true
            menu?.findItem(R.id.menu_scan)?.isVisible = false
            menu?.findItem(R.id.menu_stop)?.isVisible = true
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.menu_scan -> {
                startScan()
            }
            R.id.menu_stop -> {
                stopScan()
            }
        }
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_CANCELED) {
                finish()
                return
            } else if (resultCode == Activity.RESULT_OK) {
                returnFromResult = true
                recreate()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun listItemClick(position: Int) {
        if (mScanning) {
            stopScan()
        }

        val device: BluetoothDevice? = mListAdapter?.getDevice(position)
        device?.also {
            val intento = Intent(this, PagerActivity::class.java)
            intento.putExtra(PagerActivity.INTENT_EXTRAS_NAME, device.name)
            intento.putExtra(PagerActivity.INTENT_EXTRAS_ADDRESS, device.address)
            startActivity(intento)
        }
    }

    private fun startScan() {
        if (mScanning || !hasPermissions(bleHelper?.getBleAdapter())) {
            Log.d(Utils.TAG, "Cannot scan")
            return
        }

        mScanning = true
        mListAdapter?.clear()
        invalidateOptionsMenu()
        tvScanInstr.visibility = View.INVISIBLE

        bleHelper?.startScan()
    }

    override fun stopScan() {
        if (mScanning) {
            bleHelper?.stopScan()
            mScanning = false
            invalidateOptionsMenu()

            mListAdapter?.also {
                if (it.count > 0) {
                    tvScanInstr.visibility = View.VISIBLE
                } else {
                    Toast.makeText(this, getString(R.string.toast_no_devices_found), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // BleInteractor interface methods
    override fun hasPermissions(bleAdapter: BluetoothAdapter?): Boolean {
        if (bleAdapter == null || !bleAdapter.isEnabled) {
            Log.d(Utils.TAG, "adapter fail")
            requestBleEnable()
            return false
        } else if (!hasLocationPermission()) {
            Log.d(Utils.TAG, "location fail")
            requestLocationPermission()
            return false
        }
        return true
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
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

    override fun getContext(): Context {
        return this.applicationContext
    }

    override fun onGattDisconnect() {}

    override fun onGattConnectionResult(connect: Boolean) {}

    override fun onWriteSuccessOrFailure(result: Boolean) {}

    override fun processScanResult(result: ScanResult) {
        result.device?.apply {
            mListAdapter?.addDevice(this)
        }
    }

    inner class LeDeviceListAdapter : BaseAdapter() {

        private val deviceArray = ArrayList<BluetoothDevice>()

        fun addDevice(device: BluetoothDevice) {
            if (!deviceArray.contains(device)) {
                deviceArray.add(device)
                notifyDataSetChanged()
            }
        }

        fun getDevice(position: Int): BluetoothDevice {
            return deviceArray[position]
        }

        fun clear() {
            deviceArray.clear()
            notifyDataSetChanged()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val viewHolder: ViewHolder
            val view: View
            val inflato = layoutInflater

            if (convertView == null) {
                view = inflato.inflate(R.layout.list_item_device_scan, parent, false)
                viewHolder = ViewHolder()
                viewHolder.tvName = view.findViewById(R.id.tvListName)
                viewHolder.tvAddress = view.findViewById(R.id.tvListAddress)
                view.tag = viewHolder
            } else {
                view = convertView
                viewHolder = convertView.tag as ViewHolder
            }

            val bleDevice = deviceArray[position]
            val name = bleDevice.name
            viewHolder.tvName?.text = name ?: getString(R.string.unknown_device)
            viewHolder.tvAddress?.text = bleDevice.address.toString()

            return view
        }

        override fun getItem(position: Int): Any {
            return deviceArray[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getCount(): Int {
            return deviceArray.size
        }
    }

    private class ViewHolder {
        var tvName: TextView? = null
        var tvAddress: TextView? = null
    }

}
