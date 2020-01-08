package com.maxtauro.airdroid

import android.app.Application

class AirDroidApplication: Application() {

    var isMediaSessionServiceRunning: Boolean = false

    companion object {
        private const val TAG = "AirDroidApplication"
    }
}