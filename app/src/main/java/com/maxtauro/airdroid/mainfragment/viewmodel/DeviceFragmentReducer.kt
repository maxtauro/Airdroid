package com.maxtauro.airdroid.mainfragment.viewmodel

import android.util.Log
import com.maxtauro.airdroid.AirpodModel
import com.maxtauro.airdroid.mConnectedDevice
import com.maxtauro.airdroid.mainfragment.presenter.*

class DeviceFragmentReducer(
    private val isLocationPermissionEnabled: () -> Boolean
) {

    fun reduce(viewModel: DeviceViewModel, intent: DeviceStatusIntent): DeviceViewModel {
        Log.d(TAG, "Reducing ${intent.javaClass.name}")

        return when (intent) {
            is RefreshAirpodModelIntent -> viewModel.reduceViewModel(airpods = intent.updatedAirpods)
            is InitialScanIntent -> viewModel.reduceViewModel(isInitialScan = true)
            is InitialConnectionIntent -> viewModel.reduceViewModel(isInitialScan = true)
            is UpdateFromNotificationIntent -> viewModel.reduceViewModel(airpods = intent.airpodModel)
            is DisconnectedIntent -> DeviceViewModel.createEmptyViewModel(
                isLocationPermissionEnabled()
            )
            else -> viewModel.reduceViewModel()
        }
    }

    private fun DeviceViewModel.reduceViewModel(
        airpods: AirpodModel? = null,
        deviceName: String? = mConnectedDevice?.name,
        isInitialScan: Boolean = false
    ): DeviceViewModel {
        return this.copy(
            airpods = airpods ?: this.airpods,
            deviceName = deviceName ?: this.deviceName,
            isInitialScan = isInitialScan,
            shouldNotShowPermissionsMessage = isLocationPermissionEnabled()
        )
    }

    companion object {
        private const val TAG = "DeviceFragmentReducer"
    }
}