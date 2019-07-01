package com.example.airdroid.utils

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHeadset
import android.bluetooth.BluetoothProfile

class BluetoothUtil(private val bluetoothAdapter: BluetoothAdapter) {

    private val APPLE_MAC_ADDRESS_PREFIX = "9C:64:8B"

    fun isConnectedToHeadset(): Boolean {
        return bluetoothAdapter.getProfileConnectionState(BluetoothProfile.HEADSET) == BluetoothProfile.STATE_CONNECTED
    }

    fun isDeviceAirpods(bluetoothDevice: BluetoothDevice): Boolean {
        return bluetoothDevice.address.contains(APPLE_MAC_ADDRESS_PREFIX)
    }

    fun isPairedDevice(bluetoothDevice: BluetoothDevice): Boolean {
        val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter.bondedDevices
        return pairedDevices?.any { device -> device == bluetoothDevice } ?: return false
    }

    fun areAirpodsPaired(): Boolean {
        val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter.bondedDevices

        // TODO find cleaner way to check if a device is airpod
        return pairedDevices?.any { device -> isDeviceAirpods(device) } ?: return false
    }

    fun isConnectedDeviceAirpods(): Boolean {
        return bluetoothAdapter.getProfileConnectionState(BluetoothHeadset.HEADSET) == BluetoothHeadset.STATE_CONNECTED
    }
}