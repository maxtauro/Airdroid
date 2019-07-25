package com.example.airdroid.receivers

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.airdroid.EXTRA_DEVICE
import com.example.airdroid.MainActivity
import com.example.airdroid.mIsActivityRunning
import com.example.airdroid.mainfragment.presenter.ConnectedIntent
import com.example.airdroid.mainfragment.presenter.DeviceStatusIntent
import com.example.airdroid.mainfragment.presenter.DisconnectedIntent
import com.jakewharton.rxrelay2.PublishRelay
import org.greenrobot.eventbus.EventBus

class BluetoothConnectionReceiver : BroadcastReceiver() {

    private val eventBus = EventBus.getDefault()

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent == null) return

        val action = intent.action

        if (BluetoothDevice.ACTION_ACL_CONNECTED == action) {
            val connectedDevice = intent.extras[BluetoothDevice.EXTRA_DEVICE] as? BluetoothDevice
            connectedDevice?.let {

                Log.d(TAG, "Device Connected, Name: ${connectedDevice.name}, Address: ${connectedDevice.address}")

                if (isActivityInForegroud()) {
                    eventBus.post(ConnectedIntent(connectedDevice.name))
                } else {
                    startMainActivity(context, connectedDevice)
                }
            }
        } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED == action) {
            val disconnectedDevice = intent.extras[BluetoothDevice.EXTRA_DEVICE] as? BluetoothDevice
            disconnectedDevice?.let {
                Log.d(
                    TAG,
                    "Device Disconnected, Name: ${disconnectedDevice.name}, Address: ${disconnectedDevice.address}"
                )
            }
            eventBus.post(DisconnectedIntent)
        }
    }

    private fun isActivityInForegroud(): Boolean {
        return mIsActivityRunning
    }

    private fun startMainActivity(context: Context?, connectedDevice: BluetoothDevice) {
        Intent(context, MainActivity::class.java).also { intent ->
            intent.putExtra(EXTRA_DEVICE, connectedDevice)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context?.startActivity(intent)
        }
    }

    companion object {
        private const val TAG = "BluetoothConnectionReceiver"
    }
}