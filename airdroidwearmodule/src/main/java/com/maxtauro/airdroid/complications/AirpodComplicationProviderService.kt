package com.maxtauro.airdroid.complications

import android.support.wearable.complications.ComplicationData
import android.support.wearable.complications.ComplicationManager
import android.support.wearable.complications.ComplicationProviderService
import android.support.wearable.complications.ComplicationText
import android.util.Log
import com.maxtauro.airdroid.AirDroidApplication

class AirpodComplicationProviderService : ComplicationProviderService() {

    init {
        applicationContext as AirDroidApplication
    }

    /*
     * Called when the complication needs updated data from your provider. There are four scenarios
     * when this will happen:
     *
     *   1. An active watch face complication is changed to use this provider
     *   2. A complication using this provider becomes active
     *   3. The period of time you specified in the manifest has elapsed (UPDATE_PERIOD_SECONDS)
     *   4. You triggered an update from your own class via the
     *       ProviderUpdateRequester.requestUpdate() method.
     */
    override fun onComplicationUpdate(
        complicationId: Int, dataType: Int, complicationManager: ComplicationManager
    ) {
        Log.d(TAG, "onComplicationUpdate() id: $complicationId")


        var complicationData: ComplicationData? = when (dataType) {
            ComplicationData.TYPE_SMALL_IMAGE -> onSmallImageComplicationUpdate()
            ComplicationData.TYPE_LARGE_IMAGE -> onLargeImageComplicationUpdate()
//            ComplicationData.TYPE_SHORT_TEXT -> complicationData = ComplicationData.Builder(
//                ComplicationData.TYPE_SHORT_TEXT
//            )
//                .setShortText(ComplicationText.plainText(numberText))
//                .setTapAction(complicationPendingIntent)
//                .build()
//            ComplicationData.TYPE_LONG_TEXT -> complicationData = ComplicationData.Builder(
//                ComplicationData.TYPE_LONG_TEXT
//            )
//                .setLongText(ComplicationText.plainText("Number: $numberText"))
//                .setTapAction(complicationPendingIntent)
//                .build()
//            ComplicationData.TYPE_RANGED_VALUE -> complicationData = ComplicationData.Builder(
//                ComplicationData.TYPE_RANGED_VALUE
//            )
//                .setValue(number.toFloat())
//                .setMinValue(0f)
//                .setMaxValue(ComplicationTapBroadcastReceiver.MAX_NUMBER)
//                .setShortText(ComplicationText.plainText(numberText))
//                .setTapAction(complicationPendingIntent)
//                .build()
            else -> {
                Log.d(TAG, "Unexpected complication type $dataType")
                null
            }


        }
        if (complicationData != null) {
            complicationManager.updateComplicationData(complicationId, complicationData)
        } else {
            // If no data is sent, we still need to inform the ComplicationManager, so the update
            // job can finish and the wake lock isn't held any longer than necessary.
            complicationManager.noUpdateRequired(complicationId)
        }
    }


    private fun onSmallImageComplicationUpdate(): ComplicationData? {
        // TODO just the icon, on tap it will start the activity
        // If connected, regular icon, if not connected, an icon with an X through the airpods (or something similar)
        return ComplicationData.Builder(ComplicationData.TYPE_SMALL_IMAGE)
            .setShortText(ComplicationText.plainText("100+/100+/100+"))
            .setLongText(ComplicationText.plainText("100+/100+/100+"))
            .build()
    }

    private fun onLargeImageComplicationUpdate(): ComplicationData? {
        return ComplicationData.Builder(ComplicationData.TYPE_SMALL_IMAGE)
            .setShortText(ComplicationText.plainText("100+/100+/100+"))
            .setLongText(ComplicationText.plainText("100+/100+/100+"))
            .build()
    }

    companion object {
        private const val TAG = "AirpodComplicationProviderService"
    }
}
