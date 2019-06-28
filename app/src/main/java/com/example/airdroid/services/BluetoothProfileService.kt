package com.example.airdroid.services

import android.app.Service
import android.content.Intent
import android.os.IBinder

/**
 * This is a service that will start when airpods are connected, it is responsible for registering receivers for
 * listening to device events (battery, charging state, showing case when possible)
 **/
class BluetoothProfileService : Service() {
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}