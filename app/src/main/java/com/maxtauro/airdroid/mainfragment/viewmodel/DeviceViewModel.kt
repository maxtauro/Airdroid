package com.maxtauro.airdroid.mainfragment.viewmodel

import android.os.Parcelable
import com.maxtauro.airdroid.AirpodModel
import kotlinx.android.parcel.Parcelize

@Parcelize
data class DeviceViewModel(
    val airpods: AirpodModel,
    val deviceName: String = "",
    val isInitialScan: Boolean = false,
    val shouldNotShowPermissionsMessage: Boolean = false
) : Parcelable {


    companion object {
        @JvmStatic
        fun createEmptyViewModel(shouldShowPermissionsMessage: Boolean = false): DeviceViewModel {
            return DeviceViewModel(
                airpods = AirpodModel.EMPTY,
                shouldNotShowPermissionsMessage = shouldShowPermissionsMessage
            )
        }

    }

}