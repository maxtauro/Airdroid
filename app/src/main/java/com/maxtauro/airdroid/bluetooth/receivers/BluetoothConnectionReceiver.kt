package com.maxtauro.airdroid.bluetooth.receivers

import android.bluetooth.BluetoothA2dp
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.crashlytics.android.Crashlytics
import com.maxtauro.airdroid.EXTRA_DEVICE
import com.maxtauro.airdroid.MainActivity
import com.maxtauro.airdroid.mIsActivityRunning
import com.maxtauro.airdroid.mainfragment.presenter.ConnectedIntent
import com.maxtauro.airdroid.mainfragment.presenter.DisconnectedIntent
import com.maxtauro.airdroid.notification.NotificationService
import org.greenrobot.eventbus.EventBus

class BluetoothConnectionReceiver : BroadcastReceiver() {

    private val eventBus = EventBus.getDefault()

    private val isActivityInForegroud: Boolean
        get() = mIsActivityRunning

    private val isConnected: Boolean
        get() = BluetoothAdapter.getDefaultAdapter().getProfileConnectionState(BluetoothA2dp.HEADSET) == 1 ||
            BluetoothAdapter.getDefaultAdapter().getProfileConnectionState(BluetoothA2dp.HEADSET) == 2

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent == null) return

        val action = intent.action

        when (action) {
            BluetoothDevice.ACTION_ACL_CONNECTED -> handleBluetoothConnected(intent, context)
            BluetoothDevice.ACTION_ACL_DISCONNECTED -> handleBluetoothDisconnected(intent, context)
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_PACKAGE_INSTALL -> {
            }//TODO handle reboot
        }
    }

    private fun handleBluetoothDisconnected(intent: Intent, context: Context?) {
        val disconnectedDevice = intent.extras[BluetoothDevice.EXTRA_DEVICE] as? BluetoothDevice
        disconnectedDevice?.let {
            Log.d(
                TAG,
                "Device Disconnected, Name: ${disconnectedDevice.name}, Address: ${disconnectedDevice.address}"
            )
        }
        if (isActivityInForegroud) {
            eventBus.post(DisconnectedIntent)
        } else {
            context?.let {
                NotificationService.clearNotification(context)
                Intent(context, NotificationService::class.java).also {
                    context.stopService(it)
                }
            }
        }
    }

    private fun handleBluetoothConnected(intent: Intent, context: Context?) {
        val connectedDevice = intent.extras[BluetoothDevice.EXTRA_DEVICE] as? BluetoothDevice
        connectedDevice?.let {

            Log.d(TAG, "Device Connected, Name: ${connectedDevice.name}, Address: ${connectedDevice.address}")

            if (isActivityInForegroud) {
                eventBus.post(ConnectedIntent(connectedDevice.name))
            } else {
                startMainActivity(context, connectedDevice)
            }
        }
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