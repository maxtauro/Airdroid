package com.maxtauro.airdroid.wearablecomponents

import android.content.Context
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.android.gms.wearable.DataItem
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import com.maxtauro.airdroid.AirpodModel
import com.maxtauro.airdroid.mConnectedDevice
import com.maxtauro.airdroidcommon.*

class WearableDataManager {
    companion object {
        private const val TAG = "WearableDataManager"

        @JvmStatic
        fun sendAirpodUpdate(airpodModel: AirpodModel, context: Context) {
            // TODO do this in a corountine
            val putDataMapRequest = createPutDataMapRequest(airpodModel)
            val request = putDataMapRequest.asPutDataRequest()
            request.setUrgent()

            val dataItemTask: Task<DataItem> = Wearable.getDataClient(context).putDataItem(request)

            dataItemTask.addOnSuccessListener {
                Log.d(TAG, "Sending airpod status was successful: $it")
            }
        }

        private fun createPutDataMapRequest(airpodModel: AirpodModel): PutDataMapRequest {
            // TODO figure out how to pass this as a bundle and not as individual primitives

            return PutDataMapRequest.create(WEARABLE_DATA_AIRPOD_STATUS_PATH).setUrgent().apply {
                dataMap.putInt(WEARABLE_DATA_LEFT_CHARGE_LEVEL, airpodModel.leftAirpod.chargeLevel)
                dataMap.putInt(WEARABLE_DATA_RIGHT_CHARGE_LEVEL, airpodModel.leftAirpod.chargeLevel)
                dataMap.putInt(WEARABLE_DATA_CASE_CHARGE_LEVEL, airpodModel.leftAirpod.chargeLevel)

                dataMap.putBoolean(WEARABLE_DATA_LEFT_CHARGE_STATE, airpodModel.leftAirpod.isCharging)
                dataMap.putBoolean(WEARABLE_DATA_RIGHT_CHARGE_STATE, airpodModel.leftAirpod.isCharging)
                dataMap.putBoolean(WEARABLE_DATA_CASE_CHARGE_STATE, airpodModel.leftAirpod.isCharging)

                mConnectedDevice?.name?.let { dataMap.putString(WEARABLE_DATA_AIRPOD_NAME, it) }
            }
        }
    }
}