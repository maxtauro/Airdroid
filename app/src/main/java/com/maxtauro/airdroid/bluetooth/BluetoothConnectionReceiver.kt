package com.maxtauro.airdroid.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter.*
import android.bluetooth.BluetoothClass
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.preference.PreferenceManager
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.maxtauro.airdroid.*
import com.maxtauro.airdroid.DevicePopupActivity.DevicePopupActivity
import com.maxtauro.airdroid.DevicePopupActivity.devicepopupfragment.DevicePopupFragment
import com.maxtauro.airdroid.DevicePopupActivity.devicepopupfragment.DevicePopupFragment.Companion.EXTRA_START_FLAG
import com.maxtauro.airdroid.DevicePopupActivity.devicepopupfragment.presenter.DisconnectedIntent
import com.maxtauro.airdroid.DevicePopupActivity.devicepopupfragment.presenter.InitialConnectionIntent
import com.maxtauro.airdroid.DevicePopupActivity.mIsActivityRunning
import com.maxtauro.airdroid.notification.NotificationService
import com.maxtauro.airdroid.notification.NotificationUtil
import com.maxtauro.airdroid.wearablecomponents.WearableDataManager
import org.greenrobot.eventbus.EventBus

@SuppressLint("LongLogTag")
class BluetoothConnectionReceiver : BroadcastReceiver() {

    private val eventBus = EventBus.getDefault()

    private val isActivityInForeground: Boolean
        get() = mIsActivityRunning

    private fun isSystemAlertWindowPermissionGranted(context: Context): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Settings.canDrawOverlays(context)
        } else {
            true
        }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent == null || context == null) return

        when (intent.action) {
            BluetoothDevice.ACTION_ACL_CONNECTED -> handleBluetoothConnected(intent, context)
            BluetoothDevice.ACTION_ACL_DISCONNECTED -> handleBluetoothDisconnected(intent, context)
            ACTION_STATE_CHANGED -> handleBluetoothStateChanged(intent, context)
        }
    }

    private fun handleBluetoothConnected(intent: Intent, context: Context) {
        intent.extras?.let {
            val connectedDevice = it[BluetoothDevice.EXTRA_DEVICE] as? BluetoothDevice
            connectedDevice?.let {
                Log.d(
                    TAG,
                    "Device Connected, Name: ${connectedDevice.name}, Address: ${connectedDevice.address}"
                )

                if (!connectedDevice.isConnectedDeviceHeadset()) return

                mConnectedDevice = connectedDevice

                if (isActivityInForeground) {
                    eventBus.post(InitialConnectionIntent)
                } else {
                    handleBluetoothConnectedAppBackgrounded(context, connectedDevice)
                }
            }
        }
    }

    private fun handleBluetoothDisconnected(intent: Intent, context: Context) {
        intent.extras?.let {
            val disconnectedDevice = it[BluetoothDevice.EXTRA_DEVICE] as? BluetoothDevice
            disconnectedDevice?.let {
                Log.d(
                    TAG,
                    "Device Disconnected, Name: ${disconnectedDevice.name}, Address: ${disconnectedDevice.address}"
                )

                mConnectedDevice = null
            }
            if (isActivityInForeground) {
                eventBus.post(DisconnectedIntent)
            } else {
                FirebaseCrashlytics.getInstance().log("$TAG Stopping notification service")
                stopNotificationService(context)
            }


            WearableDataManager.sendDisconnectedUpdate(context)
        }
    }

    private fun handleBluetoothStateChanged(intent: Intent, context: Context) {
        intent.extras?.let {
            val bluetoothState = it[EXTRA_STATE] as Int

            if (bluetoothState == STATE_TURNING_OFF ||
                bluetoothState == STATE_OFF
            ) {
                handleBluetoothDisconnected(intent, context)
            }
        }
    }

    private fun handleBluetoothConnectedAppBackgrounded(
        context: Context,
        connectedDevice: BluetoothDevice
    ) {
        val preferences =
            PreferenceManager.getDefaultSharedPreferences(context)
                ?: throw IllegalStateException("Preferences haven't been initialized yet")

        val isOpenAppEnabled = preferences.getBoolean(OPEN_APP_PREF_KEY, true)
        val isNotificationEnabled = preferences.getBoolean(NOTIFICATION_PREF_KEY, true)

        if (isOpenAppEnabled &&
            context.isDeviceUnlocked() &&
            isSystemAlertWindowPermissionGranted(context)
        ) {
            startMainActivity(
                context,
                connectedDevice
            )
        } else if (isNotificationEnabled) {
            FirebaseCrashlytics.getInstance().log("$TAG  starting notification service")
            startNotificationService(context, connectedDevice)
        }
    }

    private fun stopNotificationService(context: Context) {
        Intent(context, NotificationService::class.java).also { intent ->
            context.stopService(intent)
        }
        NotificationUtil.clearNotification(context)
    }

    private fun startNotificationService(context: Context, connectedDevice: BluetoothDevice) {
        Intent(context, NotificationService::class.java).also { intent ->
            intent.putExtra(EXTRA_AIRPOD_NAME, connectedDevice.name)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }

    private fun startMainActivity(context: Context, connectedDevice: BluetoothDevice) {
        Intent(context, DevicePopupActivity::class.java).also { intent ->
            intent.putExtra(EXTRA_DEVICE, connectedDevice)
            intent.putExtra(
                EXTRA_START_FLAG,
                DevicePopupFragment.Companion.StartFlag.AIRPODS_CONNECTED
            )
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }
    }

    private fun BluetoothDevice.isConnectedDeviceHeadset() =
        this.bluetoothClass.hasService(BluetoothClass.Service.RENDER)

    companion object {

        const val EXTRA_AIRPOD_MODEL = "EXTRA_AIRPOD_MODEL"
        const val EXTRA_AIRPOD_NAME = "EXTRA_AIRPOD_NAME"

        private const val TAG = "BluetoothConnectionReceiver"
    }
}