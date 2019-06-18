package com.cloudland.blunoble.utils

import android.util.Log
import java.io.UnsupportedEncodingException
import kotlin.text.Charsets.UTF_8

class Utils {

    companion object {
        const val TAG = "something"

        // UUIDs built in to Bluno beetle
        const val SERVICE_SERIAL_UUID = "0000dfb0-0000-1000-8000-00805f9b34fb"
        const val CHARACTERISTIC_SERIAL_RXTX_UUID = "0000dfb1-0000-1000-8000-00805f9b34fb"
        const val CHARACTERISTIC_SERIAL_COMMAND_UUID = "0000dfb2-0000-1000-8000-00805f9b34fb"

        fun bytesFromString(string: String): ByteArray {
            var byteStr = ByteArray(0)
            try {
                byteStr = string.toByteArray(UTF_8)
            } catch (e: UnsupportedEncodingException) {
                Log.e(TAG, "Failed to convert message string to byte array")
            }
            return byteStr
        }

        fun stringFromBytes(bytes: ByteArray): String? {
            var stringByte: String? = null
            try {
                stringByte = String(bytes, UTF_8)
            } catch (e: UnsupportedEncodingException) {
                Log.e(TAG, "Unable to convert message bytes to string")
            }
            return stringByte
        }
    }

}