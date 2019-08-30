package com.maxtauro.airdroid.mainfragment.presenter

import com.maxtauro.airdroid.AirpodModel

sealed class DeviceStatusIntent

data class RefreshIntent(val updatedAirpods: AirpodModel) : DeviceStatusIntent()

class ConnectedIntent(val deviceName: String) : DeviceStatusIntent()

class InitialScanIntent(val deviceName: String) : DeviceStatusIntent()

class UpdateNameIntent(val deviceName: String) : DeviceStatusIntent()

object StopScanIntent : DeviceStatusIntent()

object DisconnectedIntent : DeviceStatusIntent()

