package com.example.airdroid.services

import android.app.Service
import android.bluetooth.le.ScanResult
import android.content.Intent
import android.os.IBinder
import com.example.airdroid.AirpodModel
import com.example.airdroid.callbacks.AirpodLeScanCallback
import com.example.airdroid.utils.BluetoothScannerUtil

/**
 * This is a service that will start when airpods are connected, it is responsible for registering receivers for
 * listening to device events (battery, charging state, showing case when possible)
 **/
class BluetoothScanService : Service() {

    private val recentBeacons = arrayListOf<ScanResult>()

    private var airpodLeScanCallback = AirpodLeScanCallback(recentBeacons) { broadcastUpdate(it) }
    private val scanner = BluetoothScannerUtil()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        scanner.startScan(airpodLeScanCallback)
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun broadcastUpdate(connectedAirpod: AirpodModel) {
//        TODO("Implement the result from the service")
//        val intent = Intent(action)
//        sendBroadcast(intent)
    }
}