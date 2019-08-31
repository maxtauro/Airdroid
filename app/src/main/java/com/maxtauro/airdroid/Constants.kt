package com.maxtauro.airdroid

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.util.Log

const val EXTRA_DEVICE_ADDRESS = "DEVICE_ADDRESS"
const val EXTRA_DEVICE = "EXTRA_DEVICE"

const val TEST_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"

const val SHARED_PREFERENCE_FILE_NAME = "AirDroid.SHARED_PREFERENCE_FILE_NAME"
const val NOTIFICATION_PREF_KEY = "9999"
const val OPEN_APP_PREF_KEY = "9998"

inline fun <R> R?.orElse(block: () -> R): R {
    return this ?: block()
}

fun Context?.startServiceIfDeviceUnlocked(intent: Intent) {
    val keyguardManager = this?.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
    if (!keyguardManager.isKeyguardLocked) {
        this.startService(intent)
        Log.d(this.javaClass.simpleName, "Started service with intent: $intent")
    } else Log.d(this.javaClass.simpleName, "Device locked, did not start service with intent: $intent")
}