package com.maxtauro.airdroid.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.bluetooth.BluetoothA2dp
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.VISIBILITY_PUBLIC
import com.maxtauro.airdroid.*

class NotificationUtil(
    private val context: Context,
    packageName: String
) {

    private lateinit var preferences: SharedPreferences

    private lateinit var notificationManager: NotificationManager
    private lateinit var notificationBuilder: NotificationCompat.Builder

    private lateinit var largeNotificationView: NotificationView
    private lateinit var smallNotificationView: NotificationView

    var currentNotification: Notification? = null
        private set

    private val isHeadsetConnected: Boolean
        get() {
            val connectionState = BluetoothAdapter.getDefaultAdapter()
                ?.getProfileConnectionState(BluetoothA2dp.HEADSET)
            return (connectionState == 2 || connectionState == 1)
        }

    private lateinit var airpodModel: AirpodModel

    val isNotificationEnabled: Boolean
        get() = preferences.getBoolean(NOTIFICATION_PREF_KEY, true)

    init {
        bindViews(packageName)
        initializeNotificationUtil()
    }

    private fun initializeNotificationUtil() {
        preferences = context.getSharedPreferences(
            SHARED_PREFERENCE_FILE_NAME, Context.MODE_PRIVATE
        )
            ?: throw IllegalStateException("Preferences haven't been initialized yet")

        notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationBuilder = NotificationCompat.Builder(
            context,
            TAG
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { //on oreo and newer, create a notification channel
            val channel = NotificationChannel(
                TAG,
                context.getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.enableVibration(false)
            channel.enableLights(false)
            channel.setSound(null, null)
            channel.setShowBadge(false)
            channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            notificationManager.createNotificationChannel(channel)
        }

        notificationBuilder.setVisibility(VISIBILITY_PUBLIC)

        notificationBuilder.setShowWhen(true)
        notificationBuilder.setOngoing(true)
//        notificationBuilder.setUsesChronometer(true)

        notificationBuilder.setCustomContentView(smallNotificationView)
        notificationBuilder.setCustomBigContentView(largeNotificationView)
//        notificationBuilder.setStyle(NotificationCompat.DecoratedCustomViewStyle())

        currentNotification = buildNotification(AirpodModel.EMPTY)
    }

    private fun bindViews(packageName: String) {
        largeNotificationView =
            NotificationView(isLargeNotification = true, packageName = packageName)
        smallNotificationView =
            NotificationView(isLargeNotification = false, packageName = packageName)
    }

    fun onScanResult(airpodModel: AirpodModel) {
        Log.d(TAG, "onScanResult")
        if (airpodModel.isConnected && isHeadsetConnected) {
            renderNotification(airpodModel)
        } else clearNotification(
            context
        )
    }

    private fun renderNotification(airpodModel: AirpodModel) {
        if (this::airpodModel.isInitialized && airpodModel == this.airpodModel) {
            Log.d(TAG, "Duplicate model, will not post new model")
            return
        }

        if (airpodModel.isConnected && isNotificationEnabled) {
            this.airpodModel = airpodModel
            currentNotification = buildNotification(airpodModel)
            notificationManager.notify(NOTIFICATION_ID, currentNotification)
        }
    }

    private fun buildNotification(airpodModel: AirpodModel): Notification {
        if (airpodModel.leftAirpod.isConnected && !airpodModel.rightAirpod.isConnected) {
            notificationBuilder.setSmallIcon(R.mipmap.left_airpod_notification_icon)
        } else if (!airpodModel.leftAirpod.isConnected && airpodModel.rightAirpod.isConnected) {
            notificationBuilder.setSmallIcon(R.mipmap.right_airpod_notification_icon)
        } else {
            notificationBuilder.setSmallIcon(R.mipmap.both_airpods_notification_icon)
        }

        notificationBuilder.setContentIntent(buildContentIntent(airpodModel))

        largeNotificationView.render(airpodModel)
        smallNotificationView.render(airpodModel)
        notificationBuilder.setWhen(System.currentTimeMillis())

        return notificationBuilder.build()
    }

    private fun buildContentIntent(airpodModel: AirpodModel): PendingIntent? {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        intent.putExtra(EXTRA_AIRPOD_MODEL, airpodModel)

        return PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT
        )
    }

    companion object {

        const val EXTRA_AIRPOD_MODEL = "NotificationUtil.EXTRA_AIRPOD_MODEL"
        const val EXTRA_AIRPOD_NAME = "NotificationUtil.EXTRA_AIRPOD_NAME"

        const val NOTIFICATION_ID = 1812

        private const val TAG = "NotificationUtil"

        fun clearNotification(context: Context) {
            Log.d(TAG, "Clearing notification from: $context")
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancelAll()
        }
    }
}