package com.example.airdroid

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothProfile

class BluetoothUtil(private val bluetoothAdapter: BluetoothAdapter) {

    fun isConnectedToHeadset(): Boolean {
        return bluetoothAdapter.getProfileConnectionState(BluetoothProfile.HEADSET) == BluetoothProfile.STATE_CONNECTED
    }

    fun areAirpodsPaired(): Boolean {
        val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter.bondedDevices

        // TODO find cleaner way to check if a device is airpod
        return pairedDevices?.any { device -> device.name.equals("Airpods", true) } ?: return false
    }
}