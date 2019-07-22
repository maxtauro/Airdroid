package com.example.airdroid.mainfragment.viewmodel

import com.example.airdroid.mainfragment.presenter.DeviceStatusIntent
import com.example.airdroid.mainfragment.presenter.RefreshIntent

class DeviceFragmentReducer {

    fun reduce(viewModel: DeviceViewModel, intent: DeviceStatusIntent): DeviceViewModel {
        return when (intent) {
            is RefreshIntent -> viewModel.copy(airpods = intent.updatedAirpods)
        }
    }
}