package com.maxtauro.airdroid

import android.app.Application
import com.google.android.gms.wearable.DataMap
import com.maxtauro.airdroidcommon.*
import org.greenrobot.eventbus.EventBus

class AirDroidApplication : Application() {

    var mAirpodName: String? = ""
        private set
    var mAirpodModel: AirpodModel? = null
        private set

    fun updateAirpodModel(dataMap: DataMap) {
        mAirpodModel = createAirpodModel(dataMap)
        mAirpodName = dataMap.getString(WEARABLE_DATA_AIRPOD_NAME)

        EventBus.getDefault().post(AirpodUpdateEvent)
    }

    fun clearAirpodModel() {
        mAirpodModel = null
        mAirpodName = ""
    }

    private fun createAirpodModel(dataMap: DataMap): AirpodModel {
        dataMap.apply {
            val leftChargeLevel = getInt(WEARABLE_DATA_LEFT_CHARGE_LEVEL)
            val rightChargeLevel = getInt(WEARABLE_DATA_RIGHT_CHARGE_LEVEL)
            val caseChargeLevel = getInt(WEARABLE_DATA_CASE_CHARGE_LEVEL)

            val leftChargingStatus = getBoolean(WEARABLE_DATA_LEFT_CHARGE_STATE)
            val rightChargingStatus = getBoolean(WEARABLE_DATA_RIGHT_CHARGE_STATE)
            val caseChargingStatus = getBoolean(WEARABLE_DATA_CASE_CHARGE_STATE)

            return AirpodModel.create(
                leftChargeLevel = leftChargeLevel,
                rightChargeLevel = rightChargeLevel,
                caseChargeLevel = caseChargeLevel,
                leftChargingStatus = leftChargingStatus,
                rightChargingStatus = rightChargingStatus,
                caseChargingStatus = caseChargingStatus
            )
        }
    }

    companion object {
        private const val TAG = "AirDroidApplication"
    }
}