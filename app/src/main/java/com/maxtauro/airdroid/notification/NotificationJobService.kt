package com.maxtauro.airdroid.notification

import android.app.job.JobParameters
import android.app.job.JobService
import android.bluetooth.le.ScanSettings
import android.content.Intent
import android.util.Log
import com.maxtauro.airdroid.bluetooth.callbacks.AirpodLeScanCallback
import com.maxtauro.airdroid.utils.BluetoothScannerUtil
import com.maxtauro.airdroid.utils.NotificationUtil

class NotificationJobService : JobService() {

    private lateinit var notificationUtil: NotificationUtil
    private lateinit var scanCallback: AirpodLeScanCallback

    private val scannerUtil = BluetoothScannerUtil()

    override fun onStartJob(params: JobParameters?): Boolean {
        notificationUtil = NotificationUtil(baseContext, packageName)

        if (notificationUtil.isNotificationEnabled) {
            Log.d(TAG, "Starting job")

            scanCallback = AirpodLeScanCallback(notificationUtil::onScanResult)

//            intent?.getStringExtra(NotificationService.EXTRA_AIRPOD_NAME)?.let { airpodName = it }
//            (intent?.getParcelableExtra(NotificationService.EXTRA_AIRPOD_MODEL) as? AirpodModel)?.let {
//                airpodModel = it
//                renderNotification(it)
//            }

            scannerUtil.startScan(scanCallback, ScanSettings.SCAN_MODE_LOW_POWER)

            return true
        }

        NotificationUtil.clearNotification(baseContext)
        return false
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        Log.d(TAG, "onStopJob w/ params: $params")
        scannerUtil.stopScan()
        NotificationUtil.clearNotification(baseContext)
        return true
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        NotificationUtil.clearNotification(baseContext)
        stopSelf()
        super.onTaskRemoved(rootIntent)
    }

    companion object {
        private const val TAG = "NotificationJobService"
    }
}