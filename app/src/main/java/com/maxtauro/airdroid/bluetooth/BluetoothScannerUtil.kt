package com.maxtauro.airdroid.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanSettings
import android.os.Handler
import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.jaredrummler.android.device.DeviceName


class BluetoothScannerUtil {

    private val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

    private val scanner = bluetoothAdapter?.bluetoothLeScanner
    private val scanFilters = getScanFilters()

    private lateinit var scanCallback: AirpodLeScanCallback

    var isScanning: Boolean = false
        private set

    fun startScan(
        scanCallback: AirpodLeScanCallback,
        scanMode: Int,
        disableTimeout: Boolean = false,
        timeoutCallback: () -> Unit
    ) {
        if (isScanning) return

        this.scanCallback = scanCallback
        val scanSettings = ScanSettings.Builder().setScanMode(scanMode).setReportDelay(2).build()

        Log.d(TAG, "Starting bluetooth scan")
        scanner?.startScan(scanFilters, scanSettings, scanCallback)
        isScanning = true

        if (!disableTimeout) {
            Handler().postDelayed({
                if (!scanCallback.hasFoundAirPods && isScanning) {
                    Log.d(TAG, "Bluetooth scan timed out")

                    val currentDeviceModel = DeviceName.getDeviceName()
                    FirebaseCrashlytics.getInstance()
                        .recordException(IllegalStateException("Could not get scan result for device: $currentDeviceModel"))

                    stopScan()
                    timeoutCallback()
                }
            }, 30000) // Timeout after 30s
        }
    }

    fun stopScan() {
        if (!isScanning) return

        if (!bluetoothAdapter.isBluetoothAvailable()) {
            try {
                scanner?.stopScan(scanCallback)
            } catch (e: IllegalStateException) {
                val msg = e.message + " Thrown after failing to stop scan"
                FirebaseCrashlytics.getInstance().recordException(IllegalStateException(msg))
            }

            isScanning = false
            return
        }

        check(::scanCallback.isInitialized) { "Trying to Stop scan that has no ScanCallback initialized" }

        scanner?.flushPendingScanResults(scanCallback)
        scanner?.stopScan(scanCallback)
        isScanning = false

        Log.d(TAG, "Scan stopped")
    }

    private fun getScanFilters(): List<ScanFilter> {
        val manufacturerData = ByteArray(27)
        val manufacturerDataMask = ByteArray(27)

        manufacturerData[0] = 7
        manufacturerData[1] = 25

        manufacturerDataMask[0] = -1
        manufacturerDataMask[1] = -1

        val builder = ScanFilter.Builder()
        builder.setManufacturerData(76, manufacturerData, manufacturerDataMask)
        return listOf(builder.build())
    }

    companion object {
        private const val TAG = "BluetoothScannerUtil"

        private fun BluetoothAdapter?.isBluetoothAvailable() =
            this != null && this.isEnabled && this.state == BluetoothAdapter.STATE_ON

    }
}