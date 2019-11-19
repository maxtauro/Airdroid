package com.maxtauro.airdroid.notification

import android.app.Service
import android.bluetooth.le.ScanSettings
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.crashlytics.android.Crashlytics
import com.maxtauro.airdroid.AirpodModel
import com.maxtauro.airdroid.bluetooth.AirpodLeScanCallback
import com.maxtauro.airdroid.bluetooth.BluetoothScannerUtil
import com.maxtauro.airdroid.isHeadsetConnected
import com.maxtauro.airdroid.mainfragment.presenter.RefreshAirpodModelIntent
import com.maxtauro.airdroid.orElse
import org.greenrobot.eventbus.EventBus

class NotificationService : Service() {

    private lateinit var notificationUtil: NotificationUtil
    private lateinit var scanCallback: AirpodLeScanCallback

    private val scannerUtil = BluetoothScannerUtil()

    var airpodModel: AirpodModel? = null

    override fun onCreate() {
        notificationUtil =
            NotificationUtil(baseContext, packageName)

        Crashlytics.log(Log.DEBUG, TAG, ".onCreate")
        startForeground(NotificationUtil.NOTIFICATION_ID, notificationUtil.currentNotification)
        Crashlytics.log(Log.DEBUG, TAG, " startForeground called")

        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (notificationUtil.isNotificationEnabled) {
            Crashlytics.log(Log.DEBUG, TAG, " starting notification service")
            Log.d(TAG, "Starting Notification Service")

            initializeNotification(intent)
            initializeScanner()

            return START_STICKY
        }

        stopForeground(true)
        NotificationUtil.clearNotification(baseContext)
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        Log.d(TAG, "Notification Service Stopped")
        Crashlytics.log(Log.DEBUG, TAG, " stopping notification service")
        scannerUtil.stopScan()

        if (isHeadsetConnected) {
            airpodModel?.let { EventBus.getDefault().post(RefreshAirpodModelIntent(it)) }
        }

        NotificationUtil.clearNotification(baseContext)

        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun initializeNotification(intent: Intent?) {
        intent?.extras?.getParcelable<AirpodModel>(NotificationUtil.EXTRA_AIRPOD_MODEL)?.let {
            airpodModel = it
        }.orElse {
            airpodModel = AirpodModel.EMPTY
        }

        onScanResult(airpodModel!!)
    }

    private fun initializeScanner() {
        scanCallback =
            AirpodLeScanCallback(::onScanResult, airpodModel)

        scannerUtil.startScan(
            scanCallback,
            ScanSettings.SCAN_MODE_LOW_POWER,
            airpodModel?.isConnected == true,
            ::onScanTimeout
        )
    }

    private fun onScanTimeout() {
        Log.d(TAG, "onScanTimeout")
        NotificationUtil.clearNotification(baseContext)
        stopSelf()
    }

    private fun onScanResult(airpodModel: AirpodModel) {
        Log.d(TAG, "onScanResult, notification service")
        notificationUtil.onScanResult(airpodModel)
        this.airpodModel = airpodModel
    }

    companion object {
        private const val TAG = "NotificationService"
    }
}
