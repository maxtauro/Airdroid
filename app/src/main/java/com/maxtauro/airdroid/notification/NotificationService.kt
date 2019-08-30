package com.maxtauro.airdroid.notification

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.maxtauro.airdroid.AirpodModel
import com.maxtauro.airdroid.MainActivity
import com.maxtauro.airdroid.R
import com.maxtauro.airdroid.bluetooth.callbacks.AirpodLeScanCallback
import com.maxtauro.airdroid.mainfragment.presenter.RefreshIntent
import com.maxtauro.airdroid.mainfragment.presenter.UpdateNameIntent
import com.maxtauro.airdroid.utils.BluetoothScannerUtil
import org.greenrobot.eventbus.EventBus

class NotificationService : Service() {

    private val scannerUtil = BluetoothScannerUtil()

    private val scanCallback = AirpodLeScanCallback(::onScanResult)

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
            val channel = NotificationChannel(TAG, TAG, NotificationManager.IMPORTANCE_DEFAULT)
            channel.enableVibration(false)
            channel.enableLights(false)
            channel.setSound(null, null)
            channel.setShowBadge(true)
            channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            notificationManager.createNotificationChannel(channel)
        }

        notificationBuilder.setShowWhen(false)
        notificationBuilder.setOngoing(true)

        // TODO, change the icon based on if 1 or 2 airpods connected
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

            notificationBuilder.setContentIntent(buildContentIntent(airpodModel))

            largeNotificationView.render(airpodModel)
            smallNotificationView.render(airpodModel)
            notificationManager.notify(1, notificationBuilder.build())
        } else clearNotification(baseContext)
    }

    private fun buildContentIntent(airpodModel: AirpodModel): PendingIntent? {
        val intent = Intent(applicationContext, MainActivity::class.java)
        intent.putExtra(EXTRA_AIRPOD_MODEL, airpodModel)
        return PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    companion object {

        const val EXTRA_AIRPOD_MODEL = "EXTRA_AIRPOD_MODEL"
        const val EXTRA_AIRPOD_NAME = "EXTRA_AIRPOD_NAME"

        private const val TAG = "NotificationService"

        fun clearNotification(context: Context) {
            Log.d(TAG, "Clearing notification from: $context")
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancelAll()
        }
    }
}