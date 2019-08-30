package com.maxtauro.airdroid.bluetooth.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.maxtauro.airdroid.bluetooth.services.UnlockService

/** This class acts as a starter for the UnlockService class.
 *
 *  We need to use this class for the UnlockService since we
 *  can't register USER_PRESENT in the manifest so it must
 *  be done programmatically in the UnlockService
 */
class UnlockServiceStarter : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        context?.startService(Intent(context, UnlockService::class.java))
    }
}