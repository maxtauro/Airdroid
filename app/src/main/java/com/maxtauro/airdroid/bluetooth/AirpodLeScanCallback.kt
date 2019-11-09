package com.maxtauro.airdroid.bluetooth

import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.util.Log
import com.maxtauro.airdroid.AirpodModel
import com.maxtauro.airdroid.BuildConfig


class AirpodLeScanCallback constructor(
    private val broadcastUpdate: (AirpodModel) -> Unit,
    initialAirpodModel: AirpodModel? = null
) : ScanCallback() {

    private val scanProcessor = LEScanProcessor(initialAirpodModel)

    override fun onBatchScanResults(results: List<ScanResult>) {
        for (result in results) {
            onScanResult(-1, result)
        }
        super.onBatchScanResults(results)
    }

    override fun onScanResult(unusedCallbackType: Int, result: ScanResult?) {
        if (ENABLE_SCAN_LOGGING) Log.d(
            TAG, "onScanResult with result : $result"
        )

        result?.scanRecord?.getManufacturerSpecificData(76)?.let { _ ->
            scanProcessor.processScanResult(result)
            scanProcessor.findMostLikelyCandidate()?.let {
                publishCurrentAirpodModel(it)
            }
        }
    }

    override fun onScanFailed(errorCode: Int) {
        Log.e(TAG, "Scan failed with error code: $errorCode")
        super.onScanFailed(errorCode)
    }

    private fun publishCurrentAirpodModel(it: AirpodModel) {
        Log.d(
            TAG,
            "Strongest Beacon: ${it.macAddress} with RSSI: ${it.rssi}"
        )
        broadcastUpdate(it)
    }

    companion object {
        private const val TAG = "AirpodLEScanCallback"

        private val ENABLE_SCAN_LOGGING
            get() = BuildConfig.BUILD_TYPE == "debugScanLoggingEnabled"
    }
}
