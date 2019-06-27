package com.example.airdroid

import android.app.IntentService
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder

class BluetoothService : Service() {

    //TODO inject
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private val bluetoothStateChangedIntentFilter = IntentFilter(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)
    private val bluetoothReceiver = BluetoothReceiver()

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        if (bluetoothAdapter == null) {
            stopSelf() // If the device does not support bluetooth, the service doesn't run
            return super.onStartCommand(intent, flags, startId)
        }

        registerReceiver(bluetoothReceiver, bluetoothStateChangedIntentFilter)

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(bluetoothReceiver)
    }
}
