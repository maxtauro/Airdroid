package com.maxtauro.airdroid.bluetooth.receivers

import android.annotation.SuppressLint
import android.bluetooth.BluetoothA2dp
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.maxtauro.airdroid.*
import com.maxtauro.airdroid.mainfragment.presenter.ConnectedIntent
import com.maxtauro.airdroid.mainfragment.presenter.DisconnectedIntent
import com.maxtauro.airdroid.utils.NotificationJobSchedulerUtil
import com.maxtauro.airdroid.utils.NotificationUtil
import org.greenrobot.eventbus.EventBus

@SuppressLint("LongLogTag")
class BluetoothConnectionReceiver : BroadcastReceiver() {

    private val eventBus = EventBus.getDefault()

    private val isActivityInForegroud: Boolean
        get() = mIsActivityRunning

    private val isConnected: Boolean
        get() = BluetoothAdapter.getDefaultAdapter().getProfileConnectionState(BluetoothA2dp.HEADSET) == 1 ||
                BluetoothAdapter.getDefaultAdapter().getProfileConnectionState(BluetoothA2dp.HEADSET) == 2

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent == null) return

        when (intent.action) {
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
                cancelNotificationJob(it)
                NotificationUtil.clearNotification(it)
            }
        }
    }

    private fun handleBluetoothConnected(intent: Intent, context: Context?) {
        val connectedDevice = intent.extras[BluetoothDevice.EXTRA_DEVICE] as? BluetoothDevice
        connectedDevice?.let {
            Log.d(
                TAG,
                "Device Connected, Name: ${connectedDevice.name}, Address: ${connectedDevice.address}"
            )

            if (isActivityInForegroud) {
                eventBus.post(ConnectedIntent(connectedDevice.name))
            } else {
                val preferences =
                    context?.getSharedPreferences(SHARED_PREFERENCE_FILE_NAME, Context.MODE_PRIVATE)
                        ?: throw IllegalStateException("Preferences haven't been initialized yet")

                val isOpenAppEnabled = preferences.getBoolean(OPEN_APP_PREF_KEY, true)
                val isNotificationEnabled = preferences.getBoolean(NOTIFICATION_PREF_KEY, true)

                if (isOpenAppEnabled && context.isDeviceUnlocked()) startMainActivity(
                    context,
                    connectedDevice
                )
                else if (isNotificationEnabled) scheduleNotificationJob(context, connectedDevice)
            }
        }
    }

    private fun scheduleNotificationJob(context: Context, connectedDevice: BluetoothDevice) {
        NotificationJobSchedulerUtil.scheduleJob(
            context = context,
            deviceName = connectedDevice.name
        )
    }

    private fun cancelNotificationJob(context: Context) {
        NotificationJobSchedulerUtil.cancelJob(context)
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