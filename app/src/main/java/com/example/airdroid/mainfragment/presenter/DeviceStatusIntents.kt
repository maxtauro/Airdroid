package com.example.airdroid.mainfragment.presenter

import com.example.airdroid.AirpodModel

sealed class DeviceStatusIntent

data class RefreshIntent(val updatedAirpods: AirpodModel) : DeviceStatusIntent()

class ConnectedIntent(val deviceName: String) : DeviceStatusIntent()

class InitialScanIntent(val deviceName: String) : DeviceStatusIntent()

object DisconnectedIntent : DeviceStatusIntent()

