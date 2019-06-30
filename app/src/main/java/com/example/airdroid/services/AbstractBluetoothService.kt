package com.example.airdroid.services

import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.IBinder

abstract class AbstractBluetoothService: Service(){

    //TODO inject this??
    protected val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

}