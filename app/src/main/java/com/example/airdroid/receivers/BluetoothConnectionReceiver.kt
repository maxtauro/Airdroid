package com.example.airdroid.receivers

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.airdroid.utils.BluetoothUtil

class BluetoothConnectionReceiver: BroadcastReceiver() {

    private val bluetoothAdapter =  BluetoothAdapter.getDefaultAdapter()
    private val bluetoothUtil = BluetoothUtil(bluetoothAdapter)

    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action

        if (BluetoothDevice.ACTION_ACL_CONNECTED == action) {

            if (bluetoothUtil.isConnectedDeviceAirpods()) {
                TODO("Start activity")
            }
        }

        else if (BluetoothDevice.ACTION_ACL_DISCONNECTED == action) {
            // Do I want to do something here? perhaps update the view? (but the view might just update on it's own from the activity)
        }
    }
}