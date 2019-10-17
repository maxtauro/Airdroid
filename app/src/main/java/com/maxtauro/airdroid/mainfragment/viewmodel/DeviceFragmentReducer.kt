package com.maxtauro.airdroid.mainfragment.viewmodel

import com.maxtauro.airdroid.mainfragment.presenter.*

class DeviceFragmentReducer(
    private val isLocationPermissionEnabled: () -> Boolean
) {

    fun reduce(viewModel: DeviceViewModel, intent: DeviceStatusIntent): DeviceViewModel {
        return when (intent) {
            is RefreshIntent -> viewModel.copy(
                airpods = intent.updatedAirpods,
                isInitialScan = false,
                shouldNotShowPermissionsMessage = isLocationPermissionEnabled()
            )
            is InitialScanIntent -> viewModel.copy(
                deviceName = intent.deviceName,
                isInitialScan = true,
                shouldNotShowPermissionsMessage = isLocationPermissionEnabled()
            )
            is UpdateNameIntent -> viewModel.copy(
                deviceName = intent.deviceName,
                isInitialScan = false,
                shouldNotShowPermissionsMessage = isLocationPermissionEnabled()
            )
            is DisconnectedIntent -> DeviceViewModel.createEmptyViewModel(
                isLocationPermissionEnabled()
            )
            else -> viewModel.copy(
                isInitialScan = false,
                shouldNotShowPermissionsMessage = isLocationPermissionEnabled()
            )
        }
    }
}