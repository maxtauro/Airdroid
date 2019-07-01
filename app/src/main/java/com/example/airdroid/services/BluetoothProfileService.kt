package com.example.airdroid.services

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.content.Intent
import com.example.airdroid.callbacks.AirpodGattCallback

/**
 * This is a service that will start when airpods are connected, it is responsible for registering receivers for
 * listening to device events (battery, charging state, showing case when possible)
 **/
class BluetoothProfileService : AbstractBluetoothService() {

//    private val bluetoothBatteryIntentFilter = IntentFilter("android.bluetooth.device.action.BATTERY_LEVEL_CHANGED")
//    private val bluetoothProfileReceiver = BluetoothProfileReceiver()

    private lateinit var mBluetoothDevice: BluetoothDevice
    private lateinit var bluetoothGatt: BluetoothGatt

    private var bluetoothGattCallback = AirpodGattCallback()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        if (bluetoothAdapter == null) {
            stopSelf() // If the device does not support bluetooth, the service doesn't run
            return super.onStartCommand(intent, flags, startId)
        }

        val bluetoothMACAddress = intent?.getStringExtra("DEVICE_ADDRESS")

        if (bluetoothMACAddress != null) {
            mBluetoothDevice = bluetoothAdapter.getRemoteDevice(bluetoothMACAddress)
            bluetoothGatt = mBluetoothDevice.connectGatt(this, true, bluetoothGattCallback)
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        bluetoothGatt.close()
    }
}