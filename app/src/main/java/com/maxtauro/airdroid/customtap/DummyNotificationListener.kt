package com.maxtauro.airdroid.customtap

import android.service.notification.NotificationListenerService

/**
 * For whatever reason, we need to have a notification listener service so that we can implement onActiveSessionsChanged in our
 * MediaSessionService, otherwise we can't listen for the active media session changes.
 */
class DummyNotificationListener: NotificationListenerService() {

    companion object {
        private const val TAG = "DummyNotificationListener"
    }
}