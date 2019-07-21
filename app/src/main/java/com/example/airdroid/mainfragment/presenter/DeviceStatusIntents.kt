package com.example.airdroid.mainfragment.presenter

import com.example.airdroid.mainfragment.viewmodel.AirpodViewModel

sealed class DeviceStatusIntent

data class RefreshIntent(val newViewModel: AirpodViewModel) : DeviceStatusIntent()

