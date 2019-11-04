package com.maxtauro.airdroid.bluetooth.callbacks

import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.os.SystemClock
import android.util.Log
import com.maxtauro.airdroid.AirpodModel
import com.maxtauro.airdroid.BuildConfig
import kotlin.math.absoluteValue


class AirpodLeScanCallback constructor(
    private val broadcastUpdate: (AirpodModel) -> Unit,
    private var currentAirpodModel: AirpodModel? = null
) : ScanCallback() {

    private var scanStartTime: Long? = null

    override fun onBatchScanResults(results: List<ScanResult>) {
        for (result in results) {
            onScanResult(-1, result)
        }
        super.onBatchScanResults(results)
    }

    override fun onScanResult(unusedCallbackType: Int, result: ScanResult?) {
        if (ENABLE_SCAN_LOGGING) Log.d(TAG, "onScanResult with result : $result")

        result?.scanRecord?.getManufacturerSpecificData(76)?.let {
            if (scanStartTime == null || currentAirpodModel == null) scanStartTime =
                result.timestampNanos

            updateCandidateMap(result)
            findMostLikelyAirpodModel(result)?.let { publishCurrentAirpodModel(it) }
        }
    }

    override fun onScanFailed(errorCode: Int) {
        Log.e(TAG, "Scan failed with error code: $errorCode")
        super.onScanFailed(errorCode)
    }

    fun resetStartTime() {
        scanStartTime = null
        candidateAirpodBeacons.clear()
        Log.d(TAG, "Le Scan callback reset")
    }

    private fun findMostLikelyAirpodModel(result: ScanResult): AirpodModel? {
        val resultData = result.scanRecord?.getManufacturerSpecificData(76)

        resultData?.let {

            // If this isn't one of the beacons seen in the first 10s it's definitely not our beacon
            if (!candidateAirpodBeacons.contains(result.device.address)) {
                Log.d(
                    TAG,
                    "Rudimentary Filtering, ${result.device.address} ${result.rssi} is not a valid candidate"
                )
            } else if (result.rssi <= MIN_RSSI_CANDIDATE) {
                candidateAirpodBeacons.remove(result.device.address)
                return null
            }

            val strongestBeaconResult: ScanResult? = getStrongestBeacon(result)

            if (it.size != 27 || strongestBeaconResult == null) {
                return null
            }

            val resultToProcess = strongestBeaconResult ?: result
            return resultToProcess.toAirpodModel()
        }

        return null
    }

    private fun getStrongestBeacon(result: ScanResult): ScanResult? {
        // We look for the strongest beacon in the last 10s and return the most current result for that beacon
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

    private fun publishCurrentAirpodModel(it: AirpodModel) {
        Log.d(
            TAG,
            "Strongest Beacon: ${it.macAddress} with RSSI: ${it.rssi}"
        )
        broadcastUpdate(it)
        currentAirpodModel = it
    }

    private fun updateCandidateMap(result: ScanResult) {
        result.toAirpodModel().also { airpodModel ->

            // If we see an AirPod with a  very very strong rssi, we know it is ours so we
            // clear all other potential candidates
            if (airpodModel.rssi > -40) {
                Log.d(
                    TAG,
                    "Found Airpods with very strong connection: ${result.device.address}, ${result.rssi}"
                )
                candidateAirpodBeacons.clear()
                recentBeacons.clear()
                candidateAirpodBeacons[airpodModel.macAddress] = airpodModel.lastConnected

            } else if (airpodModel.isValidCandidate()) {
                recentBeacons.add(result)
                candidateAirpodBeacons[airpodModel.macAddress] = airpodModel.lastConnected
            }

            Log.d(TAG, "Candidates: ${candidateAirpodBeacons.keys}")
        }
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

        if (lastConnected - scanStartTime!! <= INITIAL_MAX_T_NS && rssi >= MIN_RSSI) {
            return true
        }

        if (candidateAirpodBeacons.isEmpty()) {
            if (this.isSimilarToCurrentAirpodModel()) {
                Log.d(TAG, "\n $this \nis similar to\n$currentAirpodModel")
                return true
            }
            Log.d(TAG, "\n $this \n is NOT similar to \n$currentAirpodModel")
            return false
        }

        return candidateAirpodBeacons.containsKey(macAddress) ||
                rssi >= -55
    }

    private fun AirpodModel.isSimilarToCurrentAirpodModel(): Boolean {
        currentAirpodModel?.let {
            if (SystemClock.elapsedRealtimeNanos() - it.lastConnected > RECENT_BEACONS_MAX_T_NS) {
                Log.d(TAG, "Current airpod model expired ${currentAirpodModel?.macAddress}")
                currentAirpodModel = null
            }
        }

        val currentAirpodModel = currentAirpodModel

        if (currentAirpodModel == null || macAddress == currentAirpodModel.macAddress) {
            if (macAddress == currentAirpodModel?.macAddress) {
                Log.d(TAG, " \n $this \n is similar to \n $currentAirpodModel by MAC address")
            }
            return true
        }

        val isLeftPodSimilar =
            (leftAirpod.isConnected == currentAirpodModel.leftAirpod.isConnected) &&
                    (leftAirpod.chargeLevel - currentAirpodModel.leftAirpod.chargeLevel).absoluteValue <= 1

        val isRightPodSimilar =
            (rightAirpod.isConnected == currentAirpodModel.rightAirpod.isConnected) &&
                    (rightAirpod.chargeLevel - currentAirpodModel.rightAirpod.chargeLevel).absoluteValue <= 1

        return isLeftPodSimilar && isRightPodSimilar
    }

    companion object {

        private val recentBeacons = arrayListOf<ScanResult>()

        // TODO change this to be HashMap<String, AirpodModel>  (MAC -> Model)
        private var candidateAirpodBeacons = HashMap<String, Long>() // MAC address -> scan time

        private const val TAG = "AirpodLEScanCallback"
        private const val RECENT_BEACONS_MAX_T_NS = 10000000000L //10s
        private const val RECENT_BEACONS_CANDIDATE_T_NS = 10000000000L //10s
        private const val INITIAL_MAX_T_NS = 10000000000L //10s

        private const val MIN_RSSI_RELAXED = -70
        private const val MIN_RSSI_STRICT = -60

        // If we see a beacon with rssi weaker than this, we ignore it permanantly
        private const val MIN_RSSI_CANDIDATE = -75

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

            recentBeacons.removeAll {
                SystemClock.elapsedRealtimeNanos() - it.timestampNanos > RECENT_BEACONS_MAX_T_NS ||
                        !candidateAirpodBeacons.containsKey(it.device.address)
            }

        }
    }
}
