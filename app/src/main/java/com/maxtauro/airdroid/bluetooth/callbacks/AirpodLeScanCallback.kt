package com.maxtauro.airdroid.bluetooth.callbacks

import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.os.SystemClock
import android.util.Log
import androidx.annotation.VisibleForTesting
import com.maxtauro.airdroid.AirpodModel
import com.maxtauro.airdroid.BuildConfig

val recentBeacons = arrayListOf<ScanResult>()

class AirpodLeScanCallback constructor(
    private val broadcastUpdate: (AirpodModel) -> Unit
) : ScanCallback() {


    override fun onBatchScanResults(results: List<ScanResult>) {
        for (result in results) {
            onScanResult(-1, result)
        }
        super.onBatchScanResults(results)
    }

    override fun onScanResult(unusedCallbackType: Int, result: ScanResult?) {
        if (ENABLE_SCAN_LOGGING) Log.d(TAG, "onScanResult with result : $result")
        result?.let {
            val airpodResult = getAirpodModelForStrongestBeacon(result)

            airpodResult?.let {
                Log.d(TAG, "Strongest Beacon: ${airpodResult.macAddress}")
                broadcastUpdate(it)
            }
        }
    }

    private fun getAirpodModelForStrongestBeacon(result: ScanResult): AirpodModel? {
        val resultData = result.scanRecord?.getManufacturerSpecificData(76)

        resultData?.let {
            recentBeacons.add(result)

            val strongestBeaconResult: ScanResult? = getStrongestBeacon(result)

            if (strongestBeaconResult == null) Log.d(TAG, "Strongest Beacon is null")

            if ((it.size != 27 || strongestBeaconResult == null) && recentBeacons.isNotEmpty()) {
                return null
            }

            val resultToProcess = strongestBeaconResult ?: result

            val manufacturerSpecificData =
                resultToProcess.scanRecord!!.getManufacturerSpecificData(76)!!

            return AirpodModel.create(
                manufacturerSpecificData,
                resultToProcess.device.address
            )
        }

        return null
    }

    @VisibleForTesting
    internal fun getStrongestBeacon(result: ScanResult): ScanResult? {
        var strongestBeacon: ScanResult? = null
        var i = 0

        filterOldBeacons()

        while (i < recentBeacons.size) {
            if (strongestBeacon == null || strongestBeacon.rssi < recentBeacons[i].rssi) {
                strongestBeacon = recentBeacons[i]
            }
            i++
        }

        if (strongestBeacon != null && strongestBeacon.device.address == result.device.address) {
            strongestBeacon = result
        }

        if (strongestBeacon == null || strongestBeacon.rssi < MIN_RSSI) {
            return null
        }

        return strongestBeacon
    }

    override fun onScanFailed(errorCode: Int) {
        Log.e(TAG, "Scan failed with error code: $errorCode")
        super.onScanFailed(errorCode)
    }

    companion object {

        private const val TAG = "AirpodLEScanCallback"
        private const val RECENT_BEACONS_MAX_T_NS = 10000000000L //10s

        private const val MIN_RSSI_RELAXED = -75
        private const val MIN_RSSI_STRICT = -60

        // Minimum received signal strength indication that airpods can have to be considered ours
        private val MIN_RSSI: Int
            get() {
                filterOldBeacons()

                // If there are multiple airpods nearby, we make the min rssi strict
                // this will slow down the time until we get a strongest beacon but
                // will ensure that the user isn't getting invalid data
                return if (recentBeacons.distinctBy { it.device.address }.size > 1) {
                    MIN_RSSI_STRICT
                } else {
                    MIN_RSSI_RELAXED
                }
            }

        private val ENABLE_SCAN_LOGGING
            get() = BuildConfig.BUILD_TYPE == "debugScanLoggingEnabled"

        private fun filterOldBeacons() {
            recentBeacons.removeAll { SystemClock.elapsedRealtimeNanos() - it.timestampNanos > RECENT_BEACONS_MAX_T_NS }
        }
    }
}
