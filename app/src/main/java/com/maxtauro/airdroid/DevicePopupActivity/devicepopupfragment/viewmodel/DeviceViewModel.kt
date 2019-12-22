package com.maxtauro.airdroid.DevicePopupActivity.devicepopupfragment.viewmodel

import android.os.Parcelable
import com.maxtauro.airdroid.AirpodModel
import com.maxtauro.airdroid.mConnectedDevice
import kotlinx.android.parcel.Parcelize

@Parcelize
data class DeviceViewModel(
    val airpods: AirpodModel,
    val deviceName: String = "",
    val isInitialScan: Boolean = false,
    val shouldShowTimeoutToast: Boolean = false,
    val shouldNotShowPermissionsMessage: Boolean = false
) : Parcelable {


    companion object {
        @JvmStatic
        fun createEmptyViewModel(shouldShowPermissionsMessage: Boolean = false): DeviceViewModel {
            val connectedDevice = mConnectedDevice

            return if (connectedDevice != null) {
                DeviceViewModel(
                    airpods = AirpodModel.EMPTY,
                    deviceName = connectedDevice.name ?: "",
                    shouldNotShowPermissionsMessage = shouldShowPermissionsMessage
                )
            } else {
                DeviceViewModel(
                    airpods = AirpodModel.EMPTY,
                    shouldNotShowPermissionsMessage = shouldShowPermissionsMessage
                )
            }
        }

    }

}