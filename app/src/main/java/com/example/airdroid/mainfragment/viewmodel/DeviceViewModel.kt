package com.example.airdroid.mainfragment.viewmodel

import com.example.airdroid.AirpodModel

data class DeviceViewModel(
    val airpods: AirpodModel,
    val deviceName: String = "",
    val isInitialScan: Boolean = false
) {

    companion object {
        val EMPTY = DeviceViewModel(AirpodModel.EMPTY)
    }

}