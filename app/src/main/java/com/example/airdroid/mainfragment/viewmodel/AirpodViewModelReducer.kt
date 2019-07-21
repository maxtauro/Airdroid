package com.example.airdroid.mainfragment.viewmodel

import com.example.airdroid.mainfragment.presenter.DeviceStatusIntent
import com.example.airdroid.mainfragment.presenter.RefreshIntent

class AirpodViewModelReducer {

    fun reduce(viewModel: AirpodViewModel, intent: DeviceStatusIntent): AirpodViewModel {
        return when (intent) {
            is RefreshIntent -> intent.newViewModel
        }
    }
}