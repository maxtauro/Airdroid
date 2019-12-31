package com.maxtauro.airdroid.complications

import android.app.PendingIntent
import android.content.Intent
import android.graphics.drawable.Icon
import android.support.wearable.complications.ComplicationData
import android.support.wearable.complications.ComplicationManager
import android.support.wearable.complications.ComplicationProviderService
import android.support.wearable.complications.ComplicationText
import android.util.Log
import com.maxtauro.airdroid.AirDroidApplication
import com.maxtauro.airdroid.AirpodModel
import com.maxtauro.airdroid.R
import com.maxtauro.airdroid.deviceactivity.DeviceStatusActivity

class AirpodComplicationProviderService : ComplicationProviderService() {

    private val airpodModel: AirpodModel?
        get() = (applicationContext as AirDroidApplication).mAirpodModel

    override fun onComplicationUpdate(
        complicationId: Int, dataType: Int, complicationManager: ComplicationManager
    ) {
        Log.d(TAG, "onComplicationUpdate() id: $complicationId")

        val complicationData: ComplicationData? = buildComplicationData(dataType)

        if (complicationData != null) complicationManager.updateComplicationData(
            complicationId,
            complicationData
        )
        else complicationManager.noUpdateRequired(complicationId)
    }

    private fun buildComplicationData(dataType: Int): ComplicationData? {
        return when (dataType) {
            ComplicationData.TYPE_SMALL_IMAGE -> buildSmallImageComplicationData()
            ComplicationData.TYPE_LARGE_IMAGE -> buildLargeImageComplicationData()
            ComplicationData.TYPE_ICON -> buildIconComplicationData()
            else -> {
                Log.d(TAG, "Unexpected complication type $dataType")
                null
            }
        }
    }

    private fun buildIconComplicationData(): ComplicationData? {
        Log.d(TAG, "onIconComplicationUpdate")

        val iconResId = getIconResId()
        val icon = Icon.createWithResource(this, iconResId)

        return ComplicationData.Builder(ComplicationData.TYPE_ICON)
            .setIcon(icon)
            .setTapAction(buildComplicationIntent())
            .build()
    }


    private fun buildSmallImageComplicationData(): ComplicationData? {
        Log.d(TAG, "onSmallImageComplicationUpdate")

        val iconResId = getIconResId()
        val icon = Icon.createWithResource(this, iconResId)

        return ComplicationData.Builder(ComplicationData.TYPE_SMALL_IMAGE)
            .setSmallImage(icon)
            .setTapAction(buildComplicationIntent())
            .build()
    }

    private fun buildLargeImageComplicationData(): ComplicationData? {
        Log.d(TAG, "onLargeImageComplicationUpdate")

        return ComplicationData.Builder(ComplicationData.TYPE_SMALL_IMAGE)
            .setShortText(ComplicationText.plainText("100+/100+/100+"))
            .setLongText(ComplicationText.plainText("100+/100+/100+"))
            .build()
    }

    private fun buildComplicationIntent(): PendingIntent {
        val intent = Intent(this, DeviceStatusActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        return PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT
        )
    }

    private fun getIconResId() =
        if (airpodModel?.isConnected != true) {
            R.drawable.ic_no_airpods_connected
        } else R.drawable.both_airpods_notification_icon

    companion object {
        private const val TAG = "AirpodComplicationProviderService"
    }
}
