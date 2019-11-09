package com.maxtauro.airdroid.bluetooth

import android.bluetooth.le.ScanResult
import android.os.SystemClock
import android.util.Log
import com.maxtauro.airdroid.AirpodModel
import kotlin.math.absoluteValue

/** This class is responsible for processing our BLE scan results and determining
 *  which BLE beacon is most likely the correct beacon. This is a tough problem for the following reasons:
 *      - the BLE scanner picks up all Apple Bluetooth headsets (which includes beats)
 *      - we can't determine with certainty which beacon is ours since MAC addresses are randomized
 *      - we can't just make a best guess at the beacon and filter by MAC since the MAC re-randomizes
 *          every ~30s
 *
 * So this class will gather the BLE results and determine which one is most likely the correct one
 *
 * The basic algorithm will work as follows:
 *  - Ignore any beacons w/ RSSI weaker than -70/-60
 *
 *  - We will maintain a map of all beacons that are valid candidates is the past 10s
 *  - We will maintain a list of all scan results in the past 10s that correspond to valid candidates
 *  - When LEScanProcessor.findMostLikelyAirpodModel(...) is called, it will return the most likely
 *    AirPod model based on the candidate list and the recent list, we set currentAirPodModel to be this
 *
 *  Determining Candidates:
 *  - For the first 5s of the scan, anything that looks like AirPods is viewed as a potential candidate
 *    and added to the map and the list
 *  - If it is not in the first 10s, it is considered a valid candidate if it meets one of the following
 *      - RSSI stronger that -45 (in this case we actually decide that this is definitely our airpods and clear other candidates)
 *      - If our candidate list is empty and:
 *          - If our currentAirPodModel is null or
 *          - This beacon is "similar" to our currentAirPodModel (i.e. currentAirPodModel's beacon re-randomized its MAC
 *      - If this a candidate with the same MAC address exists in the map, that entry is updated to reflect the current result
 *
 *  While we are filtering for candidates, we will filter our candidateMap and recentList to remove candidates if:
 *      - Their most recent occurrence is more than 10s ago
 *      - A scan result w/ their MAC address occurs w/ RSSI weaker than -75
 *
 *  Also, if currentAirPodModel hasn't been updated in 15s we assume it was the wrong one :( In this
 *  case we set it to null and we reset the scan start time.
 */

class LEScanProcessor(
    private var currentAirpodModel: AirpodModel? = null,
    private var scanStartTime: Long? = null
) {

    // The map of LE beacons that are potentially our AirPods, the map is MAC -> AirPod Model
    private var candidateAirpodBeacons = HashMap<String, AirpodModel>()
    private val recentBeacons = arrayListOf<AirpodModel>()

    // This will be a list of the beacons we see around us.  The number of beacons will determine how
    // strict our MIN_RSSI will be
    private val distinctBeacons = HashMap<String, Long>() // TODO, do we actually need this?

    private val BEACON_EXPIRY_T_NS = 10000000000L //10s
    private val INITIAL_SCAN_PERIOD_T_NS = 5000000000L //5s
    private val CURRENT_EXPIRY_T_NS = 15000000000L //15s

    private val MIN_RSSI_RELAXED = -65
    private val MIN_RSSI_STRICT = -60
    private val MIN_RSSI_CANDIDATE = -75

    // If we see a beacon with rssi stronger than this, we take it as our AirPods
    private val MIN_RSSI_GUARANTEE = -45

    // Minimum received signal strength indication that airpods can have to be considered ours
    private val MIN_RSSI: Int
        get() {
            filterOldBeacons()

            // If there are multiple airpods nearby, we make the min rssi strict
            // this will slow down the time until we get a strongest beacon but
            // will ensure that the user isn't getting invalid data

            return if (recentBeacons
                    .distinctMaxRssi()
                    .filter { it.rssi > MIN_RSSI_RELAXED }
                    .size > 1
            ) {
                MIN_RSSI_STRICT
            } else {
                MIN_RSSI_RELAXED
            }
        }

    fun processScanResult(result: ScanResult) {
        result.toAirpodModel().let { airpodModel ->

            if (scanStartTime == null || currentAirpodModel == null) {
                scanStartTime = result.timestampNanos
                Log.d(TAG, "Reset scan start time")
            }

            filterOldBeacons()
            checkAndUpdateExpiredModel()
            updateCandidates(airpodModel)
        }
    }

    fun findMostLikelyCandidate(): AirpodModel? {
        filterOldBeacons()
        val strongestBeacon = getStrongestBeacon()
        currentAirpodModel = strongestBeacon

        return strongestBeacon
    }

    private fun getStrongestBeacon(): AirpodModel? {
        var strongestBeacon: AirpodModel? = null

        recentBeacons.forEach { beacon ->
            if (strongestBeacon == null || beacon.rssi > strongestBeacon!!.rssi)
                strongestBeacon = beacon
        }

        return if (strongestBeacon == null) null else candidateAirpodBeacons[strongestBeacon!!.macAddress]
    }

    private fun updateCandidates(airpodModel: AirpodModel) {

        if (airpodModel.rssi > MIN_RSSI_GUARANTEE) {
            processGuaranteedCandidate(airpodModel)
            return
        }

        if (airpodModel.rssi < MIN_RSSI) return

        // If we are within the first 5s of the scan
        if (airpodModel.lastConnected - scanStartTime!! < INITIAL_SCAN_PERIOD_T_NS
        ) {
            candidateAirpodBeacons[airpodModel.macAddress] = airpodModel
        } else if (airpodModel.isValidCandidate()) {
            candidateAirpodBeacons[airpodModel.macAddress] = airpodModel
        }

        if (candidateAirpodBeacons.containsKey(airpodModel.macAddress)) {
            recentBeacons.add(airpodModel)
        }
    }

    private fun processGuaranteedCandidate(airpodModel: AirpodModel) {
        candidateAirpodBeacons.clear()
        recentBeacons.clear()

        candidateAirpodBeacons[airpodModel.macAddress] = airpodModel
        recentBeacons.add(airpodModel)
        currentAirpodModel = airpodModel
    }

    private fun AirpodModel.isValidCandidate(): Boolean {
        if (this.rssi < MIN_RSSI_CANDIDATE) {
            candidateAirpodBeacons.remove(this.macAddress)
            recentBeacons.removeAll { it.macAddress == this.macAddress }

            // TODO, should we clear the model as well?

            return false
        }

        if (candidateAirpodBeacons.isEmpty() && (currentAirpodModel == null || this.isSimilarToCurrentAirpodModel())) {
            return true
        } else if (candidateAirpodBeacons.containsKey(this.macAddress)) {
            return true
        }

        return false
    }

    fun resetStartTime() {
        scanStartTime = null
        Log.d(TAG, "Scan Start time reset")
    }

    private fun filterOldBeacons() {
        // If a beacon has not been seen in the past 10s it is no longer a candidate
        val expiredBeacons = mutableListOf<String>()

        // Get the MAC address of the expired beacons
        candidateAirpodBeacons.keys.forEach {
            if (SystemClock.elapsedRealtimeNanos() - candidateAirpodBeacons[it]!!.lastConnected > BEACON_EXPIRY_T_NS) {
                expiredBeacons.add(it)
            }
        }

        // Remove the expired beacons
        expiredBeacons.forEach {
            candidateAirpodBeacons.remove(it)
        }

        // Remove the expired beacons from the recent list
        recentBeacons.removeAll {
            SystemClock.elapsedRealtimeNanos() - it.lastConnected > BEACON_EXPIRY_T_NS ||
                    !candidateAirpodBeacons.containsKey(it.macAddress)
        }

        Log.d(TAG, "Candidates: ${candidateAirpodBeacons.keys}")
    }

    private fun checkAndUpdateExpiredModel() {
        currentAirpodModel?.let {
            if (
                SystemClock.elapsedRealtimeNanos() - it.lastConnected > CURRENT_EXPIRY_T_NS &&
                !candidateAirpodBeacons.containsKey(it.macAddress)
            ) {
                Log.d(TAG, "Current airpod model expired ${currentAirpodModel?.macAddress}")
                currentAirpodModel = null
            }
        }
    }

    private fun AirpodModel.isSimilarToCurrentAirpodModel(): Boolean {
        val currentAirpodModel = currentAirpodModel

        if (currentAirpodModel == null || macAddress == currentAirpodModel.macAddress) {
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
        private const val TAG = "LEScanProcessor"

        private fun ScanResult.toAirpodModel(): AirpodModel {
            val manufacturerSpecificData =
                this.scanRecord!!.getManufacturerSpecificData(76)!!

            return AirpodModel.create(
                manufacturerSpecificData = manufacturerSpecificData,
                address = this.device.address,
                rssi = this.rssi
            )
        }

        private fun List<AirpodModel>.distinctMaxRssi(): List<AirpodModel> {
            val macRssiMap = HashMap<String, AirpodModel>()

            forEach {
                val key = it.macAddress

                if (macRssiMap[key] == null || it.rssi > macRssiMap[key]!!.rssi) {
                    macRssiMap[key] = it
                }
            }

            return ArrayList(macRssiMap.values)
        }
    }
}


