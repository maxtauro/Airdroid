package com.maxtauro.airdroid.mainfragment.viewmodel

import com.maxtauro.airdroid.mainfragment.presenter.DeviceStatusIntent
import com.maxtauro.airdroid.mainfragment.presenter.DisconnectedIntent
import com.maxtauro.airdroid.mainfragment.presenter.InitialScanIntent
import com.maxtauro.airdroid.mainfragment.presenter.RefreshIntent
import com.maxtauro.airdroid.mainfragment.presenter.UpdateNameIntent

class DeviceFragmentReducer {

    fun reduce(viewModel: DeviceViewModel, intent: DeviceStatusIntent): DeviceViewModel {
        return when (intent) {
            is RefreshIntent -> viewModel.copy(airpods = intent.updatedAirpods, isInitialScan = false)
            is InitialScanIntent -> viewModel.copy(deviceName = intent.deviceName, isInitialScan = true)
            is UpdateNameIntent -> viewModel.copy(deviceName = intent.deviceName, isInitialScan = false)
            is DisconnectedIntent -> DeviceViewModel.EMPTY
            else -> viewModel.copy(isInitialScan = false)
        }
    }
}