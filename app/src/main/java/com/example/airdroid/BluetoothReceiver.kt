package com.example.airdroid

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BluetoothReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action

        if (BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED == action) {
            TODO("start activity when airpods connected")
        }
    }
}