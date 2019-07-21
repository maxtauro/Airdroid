package com.example.airdroid.callbacks

import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.os.SystemClock
import android.util.Log
import com.example.airdroid.mainfragment.viewmodel.AirpodViewModel

class AirpodLeScanCallback(
    private val recentBeacons: ArrayList<ScanResult>,
    private val broadcastUpdate: (AirpodViewModel) -> Unit
) :
    ScanCallback() {

    override fun onBatchScanResults(results: List<ScanResult>) {
        for (result in results) {
            onScanResult(-1, result)
        }
        super.onBatchScanResults(results)
    }

    override fun onScanResult(unusedCallbackType: Int, result: ScanResult?) {
        result?.let {
            val airpodResult = getAirpodModelForStrongestBeacon(result)

            airpodResult?.let {
                broadcastUpdate(it)
            }
        }
    }

    private fun getAirpodModelForStrongestBeacon(result: ScanResult): AirpodViewModel? {
        val resultData = result.scanRecord?.getManufacturerSpecificData(76)

        resultData?.let {
            recentBeacons.add(result)

            val strongestBeaconResult: ScanResult? = getStrongestBeacon(result)

            if (it.size != 27 || strongestBeaconResult == null) return null

            val manufacturerSpecificData = strongestBeaconResult.scanRecord!!.getManufacturerSpecificData(76)!!
            return AirpodViewModel.create(manufacturerSpecificData)
        }

        return null
    }

    private fun getStrongestBeacon(result: ScanResult): ScanResult? {

        var strongestBeacon: ScanResult? = null
        var i = 0

        while (i < recentBeacons.size) {
            if (SystemClock.elapsedRealtimeNanos() - recentBeacons[i].timestampNanos > RECENT_BEACONS_MAX_T_NS) {
                recentBeacons.removeAt(i--)
                i++
                continue
            }
            if (strongestBeacon == null || strongestBeacon.rssi < recentBeacons[i].rssi) strongestBeacon =
                recentBeacons[i]
            i++
        }

        if (strongestBeacon != null && strongestBeacon.device.address == result.device.address) strongestBeacon =
            result

        if (strongestBeacon == null || strongestBeacon.rssi < -60) return null

        return strongestBeacon
    }

    override fun onScanFailed(errorCode: Int) {
        Log.e(TAG, "Scan failed with error code: $errorCode")
        super.onScanFailed(errorCode)
    }

    companion object {

        private const val TAG = "AirpodLEScanCallback"
        private const val RECENT_BEACONS_MAX_T_NS = 10000000000L //10s
    }
}