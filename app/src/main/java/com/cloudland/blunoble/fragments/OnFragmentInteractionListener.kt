package com.cloudland.blunoble.fragments


interface OnFragmentInteractionListener {

    fun unlinkBleDevice()

    fun sendCommand(command: String?)

    fun checkConnected(): Boolean

    fun openSettings()
}