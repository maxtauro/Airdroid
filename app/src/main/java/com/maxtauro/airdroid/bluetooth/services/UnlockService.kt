package com.maxtauro.airdroid.bluetooth.services

import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.Intent.ACTION_USER_PRESENT
import android.content.IntentFilter
import android.os.IBinder
import android.util.Log
import com.maxtauro.airdroid.bluetooth.receivers.UnlockReceiver

/** This Service is used to register a receiver for when the device
 *  is unlocked and airpods were connected while the device was locked
 */
class UnlockService : Service() {

    private val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

    private val unlockReceiver = UnlockReceiver()
    private var isReceiverRegistered = false

    private val userPresentIntentFilter = IntentFilter(ACTION_USER_PRESENT)

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (bluetoothAdapter == null) {
            Log.d(TAG, "Device Doesn't support bluetooth, UnlockService won't run")
            stopSelf() // If the device does not support bluetooth, the service doesn't run
            return super.onStartCommand(intent, flags, startId)
        }

        registerReceiver(unlockReceiver, userPresentIntentFilter)
        isReceiverRegistered = true
        Log.d(TAG, "Service Started")
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isReceiverRegistered) {
            unregisterReceiver(unlockReceiver)
            isReceiverRegistered = false
        }
        Log.d(TAG, "Service stopped")

    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    companion object {
        private const val TAG = "UnlockService"
    }
}