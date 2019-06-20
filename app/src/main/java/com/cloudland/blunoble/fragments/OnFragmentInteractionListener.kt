package com.cloudland.blunoble.fragments

/*
Interface for a fragment to communicate with an activity
 */
interface OnFragmentInteractionListener {

    fun unlinkBleDevice()

    fun sendCommand(command: String?)

    fun checkConnected(): Boolean

    fun openSettings()
}