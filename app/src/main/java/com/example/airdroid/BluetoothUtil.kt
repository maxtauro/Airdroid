package com.example.airdroid

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothProfile

class BluetoothUtil(private val bluetoothAdapter: BluetoothAdapter) {

    fun isConnectedToHeadset(): Boolean {
        return bluetoothAdapter.getProfileConnectionState(BluetoothProfile.HEADSET) == BluetoothProfile.STATE_CONNECTED
    }
}