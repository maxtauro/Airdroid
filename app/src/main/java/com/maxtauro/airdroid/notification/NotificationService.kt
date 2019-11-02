package com.maxtauro.airdroid.notification

import android.app.Service
import android.bluetooth.le.ScanSettings
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.google.gson.Gson
import com.maxtauro.airdroid.AirpodModel
import com.maxtauro.airdroid.bluetooth.callbacks.AirpodLeScanCallback
import com.maxtauro.airdroid.isHeadsetConnected
import com.maxtauro.airdroid.mainfragment.presenter.RefreshIntent
import com.maxtauro.airdroid.orElse
import com.maxtauro.airdroid.utils.BluetoothScannerUtil
import org.greenrobot.eventbus.EventBus

class NotificationService: Service() {

    private lateinit var notificationUtil: NotificationUtil
    private lateinit var scanCallback: AirpodLeScanCallback

    private val scannerUtil = BluetoothScannerUtil()

    var airpodModel: AirpodModel? = null

    override fun onCreate() {
        Log.d(TAG, "onCreate NotificationService")
        notificationUtil =
            NotificationUtil(baseContext, packageName)

        startForeground(NotificationUtil.NOTIFICATION_ID, notificationUtil.currentNotification)

        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (notificationUtil.isNotificationEnabled) {
            Log.d(TAG, "Starting Notification Service")

            scanCallback = AirpodLeScanCallback(::onScanResult)

            intent?.extras?.getString(NotificationUtil.EXTRA_AIRPOD_MODEL)?.let {
                airpodModel = it.jsonToAirpodModel()
            }.orElse {
                airpodModel = AirpodModel.EMPTY
            }

            onScanResult(airpodModel!!)
            scannerUtil.startScan(scanCallback, ScanSettings.SCAN_MODE_LOW_POWER)

            return START_STICKY
        }

        NotificationUtil.clearNotification(baseContext)
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        Log.d(TAG, "Notification Service Stopped")
        scannerUtil.stopScan()

        if (isHeadsetConnected) {
            airpodModel?.let { EventBus.getDefault().post(RefreshIntent(it)) }
        }

        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun onScanResult(airpodModel: AirpodModel) {
        Log.d(TAG, "onScanResult, notification service")
        notificationUtil.onScanResult(airpodModel)
    }

    private fun String.jsonToAirpodModel(): AirpodModel {
        val gson = Gson()
        return gson.fromJson(this, AirpodModel::class.java)
    }

    companion object {
        private const val TAG = "NotificationService"
    }
}