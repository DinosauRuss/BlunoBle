package com.cloudland.blunoble.activities.scan

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
import com.cloudland.blunoble.activities.main.MainActivity
import com.cloudland.blunoble.utils.Utils
import kotlinx.android.synthetic.main.activity_device_scan.*

class ScanActivity :
    AppCompatActivity(),
    ScanContract.ScanView {

    companion object {
        private var returnFromResult = false
    }

    private var mListAdapter: LeDeviceListAdapter? = null
    private var mScanPresenter: ScanContract.ScanPresenter? = null

    private val REQUEST_ENABLE_BT = 1
    private val REQUEST_FINE_LOCATION = 2


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_device_scan)

        // Verify phone is BLE capable
        // Is also set in the manifest
        packageManager.takeIf { !packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE) }?.also {
            Toast.makeText(this, getString(R.string.toast_no_ble), Toast.LENGTH_SHORT).show()
            finish()
        }

        mScanPresenter = ScanPresenter(this)

        if (returnFromResult) {
            startScan()
            returnFromResult = false
        }

        foundDevicesList.setOnItemClickListener { adapterView: AdapterView<*>, view1: View, position: Int, id: Long ->
            listItemClick(position) }
    }

    override fun onResume() {
        super.onResume()

        // Initialize list view adapter
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
        if (mScanPresenter?.isScanning() != true) {
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

    override fun onDestroy() {
        mScanPresenter?.onDestroy()
        super.onDestroy()
    }

    private fun listItemClick(position: Int) {
        if (mScanPresenter?.isScanning() == true) {
            stopScan()
        }

        val device: BluetoothDevice? = mListAdapter?.getDevice(position)
        device?.also {
            val intento = Intent(this, MainActivity::class.java)
            intento.putExtra(MainActivity.INTENT_EXTRAS_NAME, device.name)
            intento.putExtra(MainActivity.INTENT_EXTRAS_ADDRESS, device.address)
            startActivity(intento)
        }
    }

    private fun startScan() {
        if (mScanPresenter?.isScanning() == true || !hasPermissions(mScanPresenter?.getBleAdapter())) {
            Log.d(Utils.TAG, "Cannot scan")
            return
        }

        setStartScanViews()
        mScanPresenter?.startScan()
    }

    private fun hasPermissions(bleAdapter: BluetoothAdapter?): Boolean {
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

    private fun setStopScanViews() {
        invalidateOptionsMenu()
        mListAdapter?.also {
            if (it.count > 0) {
                tvScanInstr.visibility = View.VISIBLE
            } else {
                Toast.makeText(this, getString(R.string.toast_no_devices_found), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setStartScanViews() {
        mListAdapter?.clear()
        invalidateOptionsMenu()
        tvScanInstr.visibility = View.INVISIBLE
    }

    //  ScanContract interface methods
    override fun getContext(): Context {
        return this.applicationContext
    }

    override fun stopScan() {
        if (mScanPresenter?.isScanning() == true) {
            mScanPresenter?.stopScan()
        }
    }

    override fun processScanResult(result: ScanResult) {
        result.device?.apply {
            mListAdapter?.addDevice(this)
        }
    }

    override fun onStopScanCallback() {
        setStopScanViews()
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
