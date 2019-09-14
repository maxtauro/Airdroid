package com.maxtauro.airdroid

import android.app.AlertDialog
import android.app.KeyguardManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.util.Log
import com.crashlytics.android.Crashlytics

const val EXTRA_DEVICE_ADDRESS = "DEVICE_ADDRESS"

const val EXTRA_DEVICE = "EXTRA_DEVICE"
const val TEST_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"

const val SHARED_PREFERENCE_FILE_NAME = "AirDroid.SHARED_PREFERENCE_FILE_NAME"

const val NOTIFICATION_PREF_KEY = "9999"
const val OPEN_APP_PREF_KEY = "9998"
const val SHOULD_SHOW_SYSTEM_ALERT_PROMPT_KEY = "9997"


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
        Crashlytics.logException(IllegalStateException(msg))
        Log.d(this?.javaClass?.simpleName, msg)
    }
}

fun Context.showSystemAlertWindowDialog(onCancel: () -> Unit = {}) {
    AlertDialog.Builder(this)
        .setMessage(getString(R.string.display_over_other_apps))
        .setPositiveButton(getString(R.string.positive_btn_label)) { _: DialogInterface, _: Int ->
            startActivity(
                Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:$packageName")
                )
            )
        }
        .setOnCancelListener { onCancel() }
        .create()
        .show()
}
