package com.maxtauro.airdroid.mainfragment.presenter

import android.content.Context
import com.maxtauro.airdroid.AirpodModel

sealed class DeviceStatusIntent

class RefreshIntent(val updatedAirpods: AirpodModel) : DeviceStatusIntent()

class ConnectedIntent(val deviceName: String) : DeviceStatusIntent()

class InitialScanIntent(val deviceName: String) : DeviceStatusIntent()

class UpdateNameIntent(val deviceName: String) : DeviceStatusIntent()

class ScanForDeviceNameIntent(val context: Context) : DeviceStatusIntent()

object StopScanIntent : DeviceStatusIntent()

object ReRenderIntent : DeviceStatusIntent()

object DisconnectedIntent : DeviceStatusIntent()

