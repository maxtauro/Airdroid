package com.example.airdroid.receivers

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Handler
import com.example.airdroid.callbacks.AirpodScanCallback
import com.example.airdroid.services.BluetoothProfileService
import com.example.airdroid.utils.BluetoothUtil

class BluetoothConnectionReceiver : BroadcastReceiver() {

    private var mScanning: Boolean = false

    private val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private val bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner

    private val handler: Handler = Handler()

    private val SCAN_PERIOD: Long = 10000

    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action
        val leScanCallback = context?.let { AirpodScanCallback(context) }

        if (BluetoothDevice.ACTION_ACL_CONNECTED == action) {
            scanLeDevice(true, leScanCallback)

        } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED == action) {
            // Do I want to do something here? perhaps update the view? (but the view might just update on it's own from the activity)
        }
    }

    private fun scanLeDevice(enable: Boolean, leScanCallback: AirpodScanCallback?) {
        when (enable) {
            true -> {
                // Stops scanning after a pre-defined scan period.
                handler.postDelayed({
                    mScanning = false
                    bluetoothLeScanner.stopScan(leScanCallback)
                }, SCAN_PERIOD)
                mScanning = true
                bluetoothLeScanner.startScan(leScanCallback)
            }
            else -> {
                mScanning = false
                bluetoothLeScanner.stopScan(leScanCallback)
            }
        }
    }

}