package com.example.airdroid.bluetooth.receivers

import android.bluetooth.BluetoothA2dp
import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.airdroid.mIsActivityRunning
import com.example.airdroid.notification.NotificationService

class UnlockReceiver : BroadcastReceiver() {

    private val isConnected: Boolean
        get() = BluetoothAdapter.getDefaultAdapter().getProfileConnectionState(BluetoothA2dp.HEADSET) == 1 ||
            BluetoothAdapter.getDefaultAdapter().getProfileConnectionState(BluetoothA2dp.HEADSET) == 2

    private val isActivityInForegroud: Boolean
        get() = mIsActivityRunning

    override fun onReceive(context: Context?, intent: Intent?) {

        Log.d(TAG, "${intent?.action} received")

        if (isConnected && !isActivityInForegroud) {
            Intent(context, NotificationService::class.java).also {
                context?.startService(it)
            }
        }
    }

    companion object {
        private const val TAG = "UnlockReceiver"
    }
}