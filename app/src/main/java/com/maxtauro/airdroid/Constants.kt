package com.maxtauro.airdroid

import android.app.AlertDialog
import android.app.KeyguardManager
import android.bluetooth.BluetoothA2dp
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.maxtauro.airdroid.DevicePopupActivity.mIsActivityRunning

// TODO This whole class is super gross, each of these things deserves a better home

const val EXTRA_DEVICE_ADDRESS = "DEVICE_ADDRESS"

const val EXTRA_DEVICE = "EXTRA_DEVICE"
const val TEST_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"

const val SHARED_PREFERENCE_FILE_NAME = "AirDroid.SHARED_PREFERENCE_FILE_NAME"

const val NOTIFICATION_PREF_KEY = "9999"
const val OPEN_APP_PREF_KEY = "9998"
const val SHOULD_SHOW_SYSTEM_ALERT_PROMPT_KEY = "9997"
const val DARK_MODE_BY_SYSTEM_SETTINGS_PREF_KEY = "9996"
const val DARK_MODE_BY_TOGGLE_PREF_KEY = "9995"
const val HAS_MIGRATED_PREF_KEY = "9994"


inline fun <R> R?.orElse(block: () -> R): R {
    return this ?: block()
}

fun Context?.isDeviceUnlocked(): Boolean{
    val keyguardManager = this?.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
    return !keyguardManager.isKeyguardLocked
}

fun Context?.startServiceIfDeviceUnlocked(intent: Intent) {
    if (!isDeviceUnlocked() && mIsActivityRunning) {
        this?.let {
            this.startService(intent)
            Log.d(this.javaClass.simpleName, "Started service with intent: $intent")
        }
    } else {
        val msg = "Device locked, did not start service with intent: $intent"
        FirebaseCrashlytics.getInstance().recordException(IllegalStateException(msg))
        Log.d(this?.javaClass?.simpleName, msg)
    }
}

@RequiresApi(Build.VERSION_CODES.M)
fun Context.showSystemAlertWindowDialog(onCancel: () -> Unit = {}) {
    AlertDialog.Builder(this)
        .setMessage(getString(R.string.display_over_other_apps))
        .setPositiveButton(getString(R.string.positive_btn_label)) { _: DialogInterface, _: Int ->
            startActivity(
                Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
            )
        }
        .setOnCancelListener { onCancel() }
        .create()
        .show()
}

val isHeadsetConnected: Boolean
    get() {
        val connectionState = BluetoothAdapter.getDefaultAdapter()
            ?.getProfileConnectionState(BluetoothA2dp.HEADSET)
        return (connectionState == 2 || connectionState == 1)
    }
