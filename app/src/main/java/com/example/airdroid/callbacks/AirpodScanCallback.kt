package com.example.airdroid.callbacks

import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import com.example.airdroid.services.BluetoothProfileService

const val EXTRA_DEVICE_ADDRESS = "DEVICE_ADDRESS"

class AirpodScanCallback(val context: Context) : ScanCallback() {

    override fun onScanResult(callbackType: Int, result: ScanResult?) {
        result?.device?.let {
            startBluetoothProfileService(context, it)
        }
        super.onScanResult(callbackType, result)
    }

    override fun onScanFailed(errorCode: Int) {
        //TODO how do we handle this?
        super.onScanFailed(errorCode)
    }

    private fun startBluetoothProfileService(context: Context?, bluetoothDevice: BluetoothDevice) {
        Intent(context, BluetoothProfileService::class.java).also { intent ->
            intent.putExtra(EXTRA_DEVICE_ADDRESS, bluetoothDevice.address) //TODO put this w/ constants
            context?.startService(intent)
        }
    }
}


