package com.example.airdroid.mainfragment.viewmodel

import com.example.airdroid.AirpodModel

data class DeviceViewModel(
    val airpods: AirpodModel
) {

    companion object {
        val EMPTY = DeviceViewModel(AirpodModel.EMPTY)
    }

}