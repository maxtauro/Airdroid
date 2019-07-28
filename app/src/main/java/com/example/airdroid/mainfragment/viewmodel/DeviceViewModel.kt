package com.example.airdroid.mainfragment.viewmodel

import android.os.Parcelable
import com.example.airdroid.AirpodModel
import kotlinx.android.parcel.Parcelize

@Parcelize
data class DeviceViewModel(
    val airpods: AirpodModel,
    val deviceName: String = "",
    val isInitialScan: Boolean = false
) : Parcelable {

    companion object {
        val EMPTY = DeviceViewModel(AirpodModel.EMPTY)
    }

}