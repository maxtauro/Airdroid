package com.maxtauro.airdroid.bluetooth.services

import android.app.job.JobParameters
import android.app.job.JobService
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGatt.GATT_SUCCESS
import android.bluetooth.BluetoothGattCallback
import android.util.Log
import com.maxtauro.airdroid.mConnectedDevice
import com.maxtauro.airdroid.orElse

class RssiUpdateService : JobService() {

    lateinit var bluetoothGatt: BluetoothGatt

    override fun onStartJob(params: JobParameters?): Boolean {

        Log.d(TAG, "Starting $TAG")

        mConnectedDevice?.let {
            bluetoothGatt = it.connectGatt(this, false, gattCallback)

            if (!bluetoothGatt.readRemoteRssi()) TODO("BREAK")

            Log.d(TAG, "Started $TAG")

            return true
        }.orElse {
            Log.d(TAG, "No connected device stopping $TAG")
            return false
        }
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        if (::bluetoothGatt.isInitialized) {
            bluetoothGatt.disconnect()
        }

//        RssiUpdateSchedulerUtil.scheduleJob(baseContext) reschedule self

        jobFinished(params, false)
        return true
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onReadRemoteRssi(gatt: BluetoothGatt?, rssi: Int, status: Int) {
            if (status == GATT_SUCCESS) {
                Log.d(TAG, "RSSI update with value: $rssi")
                stopSelf()
            } else Log.d(TAG, "RSSI update failed with status: $status")

            super.onReadRemoteRssi(gatt, rssi, status)
        }
    }

    companion object {
        private const val TAG = "RssiUpdateService"
    }
}