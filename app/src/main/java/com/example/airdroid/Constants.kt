package com.example.airdroid

const val EXTRA_DEVICE_ADDRESS = "DEVICE_ADDRESS"
const val EXTRA_DEVICE = "EXTRA_DEVICE"

const val ACTION_GATT_CONNECTED = "com.example.bluetooth.le.ACTION_GATT_CONNECTED"
const val ACTION_GATT_DISCONNECTED = "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED"
const val ACTION_GATT_SERVICES_DISCOVERED = "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED"
const val ACTION_DATA_AVAILABLE = "com.example.bluetooth.le.ACTION_DATA_AVAILABLE"
const val EXTRA_DATA = "com.example.bluetooth.le.EXTRA_DATA"

const val UUID_MASK = "0000%s-0000-1000-8000-00805f9b34fb"
val UUID_CHARACTERISTIC_BATTERY_LEVEL = String.format(UUID_MASK, "2a19")

inline fun <R> R?.orElse(block: () -> R): R {
    return this ?: block()
}