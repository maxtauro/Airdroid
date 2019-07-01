package com.example.airdroid.receivers

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.airdroid.EXTRA_DEVICE_ADDRESS
import com.example.airdroid.services.BluetoothProfileService

class BluetoothConnectionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent == null) return

        val action = intent.action

        if (BluetoothDevice.ACTION_ACL_CONNECTED == action) {
            val connectedDevice = intent.extras[BluetoothDevice.EXTRA_DEVICE] as BluetoothDevice?
            connectedDevice?.let { startBluetoothProfileService(context, connectedDevice.address) }
        } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED == action) {
            // Do I want to do something here? perhaps update the view? (but the view might just update on it's own from the activity)
        }
    }

    private fun startBluetoothProfileService(context: Context?, bluetoothMACAddress: String) {
        Intent(context, BluetoothProfileService::class.java).also { intent ->
            intent.putExtra(EXTRA_DEVICE_ADDRESS, bluetoothMACAddress) //TODO put this w/ constants
            context?.startService(intent)
        }
    }
}