package com.maxtauro.airdroid.datalayer

import android.content.ComponentName
import android.support.wearable.complications.ProviderUpdateRequester
import android.util.Log
import com.google.android.gms.wearable.*
import com.maxtauro.airdroid.AirDroidApplication
import com.maxtauro.airdroid.complications.AirpodComplicationProviderService
import com.maxtauro.airdroidcommon.WEARABLE_DATA_AIRPOD_STATUS_PATH

class AirdroidWearableListenerService : WearableListenerService() {

    override fun onDataChanged(dataEvent: DataEventBuffer?) {
        Log.d(TAG, "onDataChanged")

        dataEvent?.forEach { event ->
            when (event.type) {
                DataEvent.TYPE_CHANGED -> processAirpodUpdate(event.dataItem)
                DataEvent.TYPE_DELETED -> processAirpodDelete(event.dataItem)
            }
        }

        requestComplicationUpdate()
        super.onDataChanged(dataEvent)
    }

    private fun requestComplicationUpdate() {
        val provider =
            ComponentName(applicationContext, AirpodComplicationProviderService::class.java)
        ProviderUpdateRequester(this, provider).requestUpdateAll()
        Log.d(TAG, "Complication update requested")
    }

    private fun processAirpodDelete(dataItem: DataItem) {
        if (isAirpodStatusRequestDataEvent(dataItem)) {
            (applicationContext as AirDroidApplication).clearAirpodModel()
        }
    }

    private fun processAirpodUpdate(dataItem: DataItem) {
        if (isAirpodStatusRequestDataEvent(dataItem)) {
            DataMapItem.fromDataItem(dataItem).dataMap.let {
                (applicationContext as AirDroidApplication).updateAirpodModel(it)
            }
        }
    }

    private fun isAirpodStatusRequestDataEvent(dataItem: DataItem) =
        dataItem.uri.path?.compareTo(WEARABLE_DATA_AIRPOD_STATUS_PATH) == 0

    companion object {
        private const val TAG = "AirdroidWearableListenerService"
    }
}