package com.example.airdroid.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.airdroid.AirpodModel
import com.example.airdroid.R
import com.example.airdroid.bluetooth.callbacks.AirpodLeScanCallback
import com.example.airdroid.mainfragment.presenter.RefreshIntent
import com.example.airdroid.mainfragment.presenter.UpdateNameIntent
import com.example.airdroid.utils.BluetoothScannerUtil
import org.greenrobot.eventbus.EventBus

class NotificationService : Service() {

    private val scannerUtil = BluetoothScannerUtil()

    private val scanCallback = AirpodLeScanCallback(arrayListOf(), ::onScanResult)

    private lateinit var notificationManager: NotificationManager
    private lateinit var notificationBuilder: NotificationCompat.Builder

    private lateinit var largeNotificationView: NotificationView
    private lateinit var smallNotificationView: NotificationView

    private lateinit var airpodName: String
    private lateinit var airpodModel: AirpodModel

    override fun onCreate() {
        super.onCreate()

        bindViews()

        notificationManager = baseContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationBuilder = NotificationCompat.Builder(baseContext, TAG)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { //on oreo and newer, create a notification channel
            val channel = NotificationChannel(TAG, TAG, NotificationManager.IMPORTANCE_LOW)
            channel.enableVibration(false)
            channel.enableLights(false)
            channel.setShowBadge(true)
            channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            notificationManager.createNotificationChannel(channel)
        }

        notificationBuilder.setShowWhen(false)
        notificationBuilder.setOngoing(true)
        notificationBuilder.setSmallIcon(R.mipmap.notification_icon)

        notificationBuilder.setCustomContentView(smallNotificationView)
        notificationBuilder.setCustomBigContentView(largeNotificationView)
    }

    private fun bindViews() {
        largeNotificationView = NotificationView(isLargeNotification = true, packageName = packageName)
        smallNotificationView = NotificationView(isLargeNotification = false, packageName = packageName)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Starting NotificationService")

        intent?.getStringExtra(EXTRA_AIRPOD_NAME)?.let { airpodName = it }
        (intent?.getParcelableExtra(EXTRA_AIRPOD_MODEL) as? AirpodModel)?.let {
            airpodModel = it
            renderNotification(it)
        }

        scannerUtil.startScan(scanCallback)

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()

        // TODO find a way to do this without using event bus
        if (::airpodModel.isInitialized) EventBus.getDefault().post(RefreshIntent(airpodModel))
        if (::airpodName.isInitialized) EventBus.getDefault().post(UpdateNameIntent(airpodName))

        scannerUtil.stopScan()
    }

    private fun onScanResult(airpodModel: AirpodModel) {
        if (scannerUtil.isScanning) {
            renderNotification(airpodModel)
        }
    }

    private fun renderNotification(airpodModel: AirpodModel) {
        if (airpodModel.isConnected) {
            this.airpodModel = airpodModel
            largeNotificationView.render(airpodModel)
            smallNotificationView.render(airpodModel)
            notificationManager.notify(1, notificationBuilder.build())
        } else clearNotification(baseContext)
    }

    companion object {

        const val EXTRA_AIRPOD_MODEL = "EXTRA_AIRPOD_MODEL"
        const val EXTRA_AIRPOD_NAME = "EXTRA_AIRPOD_NAME"

        private const val TAG = "NotificationService"

        fun clearNotification(context: Context) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancelAll()
        }
    }
}