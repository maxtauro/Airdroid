package com.maxtauro.airdroid

import android.os.SystemClock
import android.util.Log
import com.maxtauro.airdroid.DevicePopupActivity.devicepopupfragment.presenter.RefreshAirpodModelIntent
import com.maxtauro.airdroid.DevicePopupActivity.devicepopupfragment.viewmodel.DeviceFragmentReducer
import com.maxtauro.airdroid.DevicePopupActivity.devicepopupfragment.viewmodel.DeviceViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import org.junit.Assert.assertEquals
import org.junit.Test

class DeviceFragmentReducerTest {

    private var isLocationPermissionEnabled = false
    private val reducer = DeviceFragmentReducer(::isLocationPermissionEnabledMock)

    init {
        mockkStatic(SystemClock::class)
        every { SystemClock.elapsedRealtimeNanos() } returns 1000L

        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0

        mConnectedDevice = mockk()
        every { mConnectedDevice!!.name } returns "Test Name"
    }

    @Test
    fun `reducing with RefreshAirpodModelIntent updates the view model correctly`() {
        val oldAirpodModel = AirpodModel.EMPTY

        val deviceViewModel = DeviceViewModel(
            airpods = oldAirpodModel,
            deviceName = "SomeName",
            isInitialScan = true,
            shouldNotShowPermissionsMessage = true,
            shouldShowTimeoutToast = false
        )

        val updatedAirPods = createAirPodModel()
        val updatedModel = reducer.reduce(deviceViewModel, RefreshAirpodModelIntent(updatedAirPods))

        assertEquals(updatedAirPods, updatedModel.airpods)
        assertEquals(mConnectedDevice!!.name, updatedModel.deviceName)
        assertEquals(false, updatedModel.isInitialScan)
        assertEquals(false, updatedModel.shouldNotShowPermissionsMessage)
        assertEquals(false, updatedModel.shouldShowTimeoutToast)
    }

    private fun createAirPodModel() = AirpodModel(
        AirpodPiece(
            chargeLevel = 80,
            isCharging = true,
            isConnected = true,
            whichPiece = WhichPiece.LEFT,
            shouldShowBatteryInfo = true
        ),
        AirpodPiece(
            chargeLevel = 30,
            isCharging = true,
            isConnected = true,
            whichPiece = WhichPiece.CASE,
            shouldShowBatteryInfo = true
        ),
        AirpodPiece(
            chargeLevel = 10,
            isCharging = true,
            isConnected = true,
            whichPiece = WhichPiece.RIGHT,
            shouldShowBatteryInfo = true
        )
    )

    private fun isLocationPermissionEnabledMock() = isLocationPermissionEnabled

    companion object {
        private const val TAG = "DeviceFragmentReducerTest"
    }
}