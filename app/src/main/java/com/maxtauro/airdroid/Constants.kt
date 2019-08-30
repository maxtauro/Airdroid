package com.maxtauro.airdroid

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.util.Log

const val EXTRA_DEVICE_ADDRESS = "DEVICE_ADDRESS"
const val EXTRA_DEVICE = "EXTRA_DEVICE"

const val ACTION_GATT_CONNECTED = "com.maxtauro.bluetooth.le.ACTION_GATT_CONNECTED"
const val ACTION_GATT_DISCONNECTED = "com.maxtauro.bluetooth.le.ACTION_GATT_DISCONNECTED"
const val ACTION_GATT_SERVICES_DISCOVERED = "com.maxtauro.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED"
const val ACTION_DATA_AVAILABLE = "com.maxtauro.bluetooth.le.ACTION_DATA_AVAILABLE"
const val EXTRA_DATA = "com.maxtauro.bluetooth.le.EXTRA_DATA"

const val TEST_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"

const val UUID_MASK = "0000%s-0000-1000-8000-00805f9b34fb"
val UUID_CHARACTERISTIC_BATTERY_LEVEL = String.format(UUID_MASK, "2a19")

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