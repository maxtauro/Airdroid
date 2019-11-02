package com.maxtauro.airdroid.bluetooth.callbacks

import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.os.SystemClock
import android.util.Log
import androidx.annotation.VisibleForTesting
import com.maxtauro.airdroid.AirpodModel
import com.maxtauro.airdroid.BuildConfig
import kotlin.math.absoluteValue


class AirpodLeScanCallback constructor(
    private val broadcastUpdate: (AirpodModel) -> Unit
) : ScanCallback() {

    private var scanStartTime: Long? = null
    private var currentAirpodModel: AirpodModel? = null

    override fun onBatchScanResults(results: List<ScanResult>) {
        for (result in results) {
            onScanResult(-1, result)
        }
        super.onBatchScanResults(results)
    }

    override fun onScanResult(unusedCallbackType: Int, result: ScanResult?) {
        if (ENABLE_SCAN_LOGGING) Log.d(TAG, "onScanResult with result : $result")

        result?.let {
            if (scanStartTime == null || candidateAirpodBeacons.isEmpty()) scanStartTime =
                result.timestampNanos

            result.toAirpodModel().also { airpodModel ->
                if (airpodModel.isValidCandidate()) {
                    candidateAirpodBeacons[airpodModel.macAddress] = airpodModel.lastConnected
                }
            }

            Log.d(TAG, "Candidates: ${candidateAirpodBeacons.keys}")


            currentAirpodModel = getAirpodModelForStrongestBeacon(result)

            currentAirpodModel?.let {
                Log.d(
                    TAG,
                    "Strongest Beacon: ${it.macAddress} with RSSI: ${it.rssi}"
                )
                broadcastUpdate(it)
            }
        }
    }

    fun resetStartTime() {
        scanStartTime = null
        candidateAirpodBeacons.clear()
        Log.d(TAG, "Le Scan callback reset")
    }

    private fun getAirpodModelForStrongestBeacon(result: ScanResult): AirpodModel? {
        val resultData = result.scanRecord?.getManufacturerSpecificData(76)

        resultData?.let {

            // If this isn't one of the beacons seen in the first 10s it's definitely not our beacon
            if (!candidateAirpodBeacons.contains(result.device.address)) {
                Log.d(
                    TAG,
                    "Rudimentary Filtering, ${result.device.address} is not a valid candidate"
                )
                return null
            } else if (result.rssi < MIN_RSSI_CANDIDATE) {
                candidateAirpodBeacons.remove(result.device.address)
                return null
            }

            recentBeacons.add(result)

            val strongestBeaconResult: ScanResult? = getStrongestBeacon(result)

            if (strongestBeaconResult == null) Log.d(TAG, "Strongest Beacon is null")

            if ((it.size != 27 || strongestBeaconResult == null) && recentBeacons.isNotEmpty()) {
                return null
            }

            val resultToProcess = strongestBeaconResult ?: result
            return resultToProcess.toAirpodModel()
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

    private fun ScanResult.toAirpodModel(): AirpodModel {
        val manufacturerSpecificData =
            this.scanRecord!!.getManufacturerSpecificData(76)!!

        return AirpodModel.create(
            manufacturerSpecificData = manufacturerSpecificData,
            address = this.device.address,
            rssi = this.rssi
        )
    }

    private fun AirpodModel.isValidCandidate(): Boolean {
        if (rssi < MIN_RSSI) return false

        if (candidateAirpodBeacons.isEmpty()) {
            if (this.isSimilar(currentAirpodModel)) {
                Log.d(TAG, "$this is similar to $currentAirpodModel")
                return true
            }
            Log.d(TAG, "$this is NOT similar to $currentAirpodModel")

        }

        return (lastConnected - scanStartTime!! <= INITIAL_MAX_T_NS && rssi > MIN_RSSI_CANDIDATE) ||
                candidateAirpodBeacons.containsKey(macAddress) ||
                rssi >= -55
    }

    private fun AirpodModel.isSimilar(currentAirpodModel: AirpodModel?): Boolean {
        if (currentAirpodModel == null || macAddress == currentAirpodModel.macAddress) return true

        val isLeftPodSimilar =
            (leftAirpod.isConnected == currentAirpodModel.leftAirpod.isConnected) &&
                    (leftAirpod.chargeLevel - currentAirpodModel.leftAirpod.chargeLevel).absoluteValue <= 2

        val isRightPodSimilar =
            (rightAirpod.isConnected == currentAirpodModel.rightAirpod.isConnected) &&
                    (rightAirpod.chargeLevel - currentAirpodModel.rightAirpod.chargeLevel).absoluteValue <= 2

        return isLeftPodSimilar && isRightPodSimilar
    }

    companion object {

        private val recentBeacons = arrayListOf<ScanResult>()
        private var candidateAirpodBeacons = HashMap<String, Long>()

        private const val TAG = "AirpodLEScanCallback"
        private const val RECENT_BEACONS_MAX_T_NS = 10000000000L //10s
        private const val RECENT_BEACONS_CANDIDATE_T_NS = 10000000000L //10s
        private const val INITIAL_MAX_T_NS = 10000000000L //10s

        private const val MIN_RSSI_RELAXED = -75
        private const val MIN_RSSI_STRICT = -60

        // If we see a beacon with rssi weaker than this, we ignore it permanantly
        private const val MIN_RSSI_CANDIDATE = -80

        // Minimum received signal strength indication that airpods can have to be considered ours
        private val MIN_RSSI: Int
            get() {
                filterOldBeacons()

                // If there are multiple airpods nearby, we make the min rssi strict
                // this will slow down the time until we get a strongest beacon but
                // will ensure that the user isn't getting invalid data
                return if (recentBeacons
                        .distinctBy { it.device.address }
                        .filter { it.rssi > MIN_RSSI_RELAXED }
                        .size > 1
                ) {
                    MIN_RSSI_STRICT
                } else {
                    MIN_RSSI_RELAXED
                }
            }

        private val ENABLE_SCAN_LOGGING
            get() = BuildConfig.BUILD_TYPE == "debugScanLoggingEnabled"

        private fun filterOldBeacons() {
            recentBeacons.removeAll { SystemClock.elapsedRealtimeNanos() - it.timestampNanos > RECENT_BEACONS_MAX_T_NS }

            // if a beacon has not been seen in the past 5s it is no longer a candidate
            val expiredBeacons = mutableListOf<String>()

            candidateAirpodBeacons.keys.forEach {
                if (SystemClock.elapsedRealtimeNanos() - candidateAirpodBeacons[it]!! > RECENT_BEACONS_CANDIDATE_T_NS) {
                    expiredBeacons.add(it)
                }
            }

            expiredBeacons.forEach {
                candidateAirpodBeacons.remove(it)
            }

        }
    }
}
