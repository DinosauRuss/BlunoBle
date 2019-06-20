package com.cloudland.blunoble.utils

import android.app.Activity
import android.content.res.Resources
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.view.inputmethod.InputMethodManager


class Utils {

    companion object {
        const val TAG = "something"

        // UUIDs built in to Bluno beetle
        const val SERVICE_SERIAL_UUID = "0000dfb0-0000-1000-8000-00805f9b34fb"
        const val CHARACTERISTIC_SERIAL_RXTX_UUID = "0000dfb1-0000-1000-8000-00805f9b34fb"
        const val CHARACTERISTIC_SERIAL_COMMAND_UUID = "0000dfb2-0000-1000-8000-00805f9b34fb"

        fun spToPx(sp: Int): Int {
            return Math.round(Resources.getSystem().displayMetrics.scaledDensity * sp)
        }

        fun dpToPx(dp: Int): Int {
            return Math.round(Resources.getSystem().displayMetrics.density * dp)
        }

        fun closeSoftKeyboard(activity: AppCompatActivity?) {
            try {
                val input =
                    activity?.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager?
                input?.hideSoftInputFromWindow(activity?.currentFocus?.windowToken, 0)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

}