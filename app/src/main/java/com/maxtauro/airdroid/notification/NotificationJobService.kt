package com.maxtauro.airdroid.notification

import android.app.job.JobParameters
import android.app.job.JobService
import android.bluetooth.le.ScanSettings
import android.content.Intent
import android.util.Log
import com.google.gson.Gson
import com.maxtauro.airdroid.AirpodModel
import com.maxtauro.airdroid.bluetooth.callbacks.AirpodLeScanCallback
import com.maxtauro.airdroid.mainfragment.presenter.RefreshIntent
import com.maxtauro.airdroid.mainfragment.presenter.UpdateNameIntent
import com.maxtauro.airdroid.notification.NotificationUtil.Companion.EXTRA_AIRPOD_MODEL
import com.maxtauro.airdroid.notification.NotificationUtil.Companion.EXTRA_AIRPOD_NAME
import com.maxtauro.airdroid.utils.BluetoothScannerUtil
import org.greenrobot.eventbus.EventBus


class NotificationJobService : JobService() {

    private lateinit var notificationUtil: NotificationUtil
    private lateinit var scanCallback: AirpodLeScanCallback

    private val scannerUtil = BluetoothScannerUtil()

    var airpodName: String? = null
    var airpodModel: AirpodModel? = null

    override fun onStartJob(params: JobParameters?): Boolean {
        notificationUtil =
            NotificationUtil(baseContext, packageName)

        if (notificationUtil.isNotificationEnabled) {
            Log.d(TAG, "Starting job")

            scanCallback = AirpodLeScanCallback(::onScanResult)

            params?.extras?.getString(EXTRA_AIRPOD_NAME)?.let { airpodName = it }
            params?.extras?.getString(EXTRA_AIRPOD_MODEL)?.let {
                airpodModel = it.jsonToAirpodModel()
                onScanResult(airpodModel!!)
            }

            scannerUtil.startScan(scanCallback, ScanSettings.SCAN_MODE_LOW_POWER)

            return true
        }

        NotificationUtil.clearNotification(baseContext)
        return false
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        Log.d(TAG, "onStopJob w/ params: $params")
        scannerUtil.stopScan()
        jobFinished(params, false)
        NotificationUtil.clearNotification(baseContext)

        airpodModel?.let { EventBus.getDefault().post(RefreshIntent(it)) }
        airpodName?.let { EventBus.getDefault().post(UpdateNameIntent(it)) }

        return true
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        scannerUtil.stopScan()
        NotificationJobSchedulerUtil.cancelJob(baseContext)
        NotificationUtil.clearNotification(baseContext)
        stopSelf()
        super.onTaskRemoved(rootIntent)
    }

    private fun onScanResult(airpodModel: AirpodModel) {
        Log.d(TAG, "onScanResult")
        notificationUtil.onScanResult(airpodModel)
    }

    private fun String.jsonToAirpodModel(): AirpodModel {
        val gson = Gson()
        return gson.fromJson(this, AirpodModel::class.java)
    }

    companion object {
        private const val TAG = "NotificationJobService"
    }
}