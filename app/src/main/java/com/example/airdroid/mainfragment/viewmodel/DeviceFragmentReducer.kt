package com.example.airdroid.mainfragment.viewmodel

import com.example.airdroid.mainfragment.presenter.DeviceStatusIntent
import com.example.airdroid.mainfragment.presenter.DisconnectedIntent
import com.example.airdroid.mainfragment.presenter.InitialScanIntent
import com.example.airdroid.mainfragment.presenter.RefreshIntent

class DeviceFragmentReducer {

    fun reduce(viewModel: DeviceViewModel, intent: DeviceStatusIntent): DeviceViewModel {
        return when (intent) {
            is RefreshIntent -> viewModel.copy(airpods = intent.updatedAirpods, isInitialScan = false)
            is InitialScanIntent -> viewModel.copy(deviceName = intent.deviceName, isInitialScan = true)
            is DisconnectedIntent -> DeviceViewModel.EMPTY
            else -> viewModel
        }
    }
}