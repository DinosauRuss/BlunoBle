package com.cloudland.blunoble.activities.main

import android.bluetooth.BluetoothAdapter
import android.content.Context
import com.cloudland.blunoble.bleUtils.BleConnectionHelper
import com.cloudland.blunoble.bleUtils.BleInteractor

class MainPresenter(private var mainView: MainContract.MainView?, address: String, connectState: Boolean):
    MainContract.MainPresenter,
    BleInteractor.Connector {

    private val bleConnectionHelper = BleConnectionHelper(
        this,
        address,
        connectState)


    // MainContract.Presenter methods
    override fun onDestroy() {
        mainView = null
    }

    override fun getBleAdapter(): BluetoothAdapter? {
        return bleConnectionHelper.getBleAdapter()
    }

    override fun disconnect() {
        bleConnectionHelper.disconnectGattServer()
    }

    override fun sendValueToDevice(command: String?) {
        bleConnectionHelper.sendValueToDevice(command)
    }


    // BleInteractor.Connector methods
    override fun getContext(): Context? {
        return mainView?.getContext()
    }

    override fun onGattDisconnect() {
        mainView?.onDisconnectCallback()
    }

    override fun onGattConnectionResult(connect: Boolean) {
        mainView?.onConnectionCallback(connect)
    }

    override fun onWriteSuccessOrFailure(result: Boolean) {
        mainView?.onWriteSuccess(result)
    }

    override fun hasPermissions(bleAdapter: BluetoothAdapter?): Boolean? {
        return mainView?.hasPermissions(bleAdapter)
    }


}