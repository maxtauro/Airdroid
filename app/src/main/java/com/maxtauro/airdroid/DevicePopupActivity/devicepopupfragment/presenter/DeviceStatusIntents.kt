package com.maxtauro.airdroid.DevicePopupActivity.devicepopupfragment.presenter

import com.maxtauro.airdroid.AirpodModel

sealed class DeviceStatusIntent


class RefreshAirpodModelIntent(val updatedAirpods: AirpodModel) : DeviceStatusIntent()

class UpdateFromNotificationIntent(val airpodModel: AirpodModel) : DeviceStatusIntent()

object StopScanIntent : DeviceStatusIntent()

object ReRenderIntent : DeviceStatusIntent()

object DisconnectedIntent : DeviceStatusIntent()

object InitialConnectionIntent : DeviceStatusIntent()

object InitialScanIntent : DeviceStatusIntent()

object StartScanIntent : DeviceStatusIntent()
object ScanTimeoutIntent : DeviceStatusIntent()

object ScanTimeoutToastShownIntent : DeviceStatusIntent()

