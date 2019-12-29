package com.maxtauro.airdroid

import android.util.Log
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService
import com.maxtauro.airdroidcommon.WEARABLE_DATA_AIRPOD_STATUS_PATH

class AirdroidWearableListenerService : WearableListenerService() {

    override fun onDataChanged(dataEvent: DataEventBuffer?) {
        Log.d(TAG, "onDatachanged")

        dataEvent?.forEach { event ->
            // DataItem changed
            if (event.type == DataEvent.TYPE_CHANGED) {
                event.dataItem.also { item ->
                    if (item.uri.path?.compareTo(WEARABLE_DATA_AIRPOD_STATUS_PATH) == 0) {
                        DataMapItem.fromDataItem(item).dataMap.let {
                            (applicationContext as AirDroidApplication).updateAirpodModel(it)
                        }
                    }
                }
            } else if (event.type == DataEvent.TYPE_DELETED) {
                TODO("Implement on Airpods disconnected")
                // DataItem deleted
            }

        }
        super.onDataChanged(dataEvent)
    }

    companion object {
        private const val TAG = "AirdroidWearableListenerService"
    }
}