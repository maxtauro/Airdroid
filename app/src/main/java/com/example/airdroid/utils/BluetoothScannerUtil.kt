package com.example.airdroid.utils

import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.util.Log

class BluetoothScannerUtil {

    private val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

    private val scanner = bluetoothAdapter?.bluetoothLeScanner
    private val scanSettings = ScanSettings.Builder().setScanMode(2).setReportDelay(2).build()
    private val scanFilters = getScanFilters()

    // TODO make set private
    var isScanning:Boolean = false
    private set(isScanning: Boolean) {
        field = isScanning
    }

    fun startScan(scanCallback: ScanCallback) {
        Log.d(TAG, "Starting bluetooth scan")
        scanner?.startScan(scanFilters, scanSettings, scanCallback)
        isScanning = true
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

    fun stopScan() {
        scanner?.stopScan(object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
            }
        })

        isScanning = false
    }

    companion object {
        private const val TAG = "BluetoothScannerUtil"
    }
}