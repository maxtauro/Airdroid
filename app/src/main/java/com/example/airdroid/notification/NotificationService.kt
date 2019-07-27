package com.example.airdroid.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.airdroid.R
import com.example.airdroid.mainfragment.viewmodel.DeviceViewModel

class NotificationService(context: Context, packageName: String) {

    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val notificationBuilder = NotificationCompat.Builder(context, TAG)

    val largeNotification = NotificationView(isLargeNotification = true, packageName = packageName)
    val smallNotification = NotificationView(isLargeNotification = false, packageName = packageName)

    init {
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

        notificationBuilder.setCustomContentView(smallNotification)
        notificationBuilder.setCustomBigContentView(largeNotification)
    }

    fun renderNotification(viewModel: DeviceViewModel) {
        if (viewModel.airpods.isConnected) {
            largeNotification.render(viewModel)
            smallNotification.render(viewModel)
            notificationManager.notify(1, notificationBuilder.build())
        }
    }

    companion object {

        private const val TAG = "NotificationService"

        fun clearNotification(context: Context) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancelAll()
        }
    }
}