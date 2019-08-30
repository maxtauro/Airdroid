package com.maxtauro.airdroid.bluetooth

import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanRecord
import android.bluetooth.le.ScanResult
import android.util.SparseArray
import io.mockk.every
import io.mockk.mockk
import org.junit.Test


class AirpodLeScanCallbackTest {

    @Test
    fun `Test Stub`() {

    }

    private fun createScanResult(
        leftChargeLevel: Int,
        rightChargeLevel: Int,
        caseChargeLevel: Int,
        leftChargeStatus: Boolean,
        rightChargeStatus: Boolean,
        caseChargeStatus: Boolean,
        isLeftConnected: Boolean,
        isRightConnected: Boolean,
        isCaseConnected: Boolean,
        rssi: Int = 20,
        timeStamp: Long = 10L,
        deviceName: String = "",
        macAddress: String = DEFAULT_MAC_ADDRESS
    ): ScanResult {

        val device: BluetoothDevice = mockk()

        val manufacturerData = createManufacturerData(
            leftChargeLevel,
            rightChargeLevel,
            caseChargeLevel,
            leftChargeStatus,
            rightChargeStatus,
            caseChargeStatus,
            isLeftConnected,
            isRightConnected,
            isCaseConnected

        )

        val scanRecord: ScanRecord = mockk()

        every { device.address } returns macAddress

        return ScanResult(device, -1, -1, -1, -1, -1, rssi, -1, scanRecord, timeStamp).apply {
            every { scanRecord.deviceName } returns deviceName
            every { scanRecord.manufacturerSpecificData } returns manufacturerData
        }

    }

    private fun createManufacturerData(
        leftChargeLevel: Int,
        rightChargeLevel: Int,
        caseChargeLevel: Int,
        leftChargeStatus: Boolean,
        rightChargeStatus: Boolean,
        caseChargeStatus: Boolean,
        leftConnected: Boolean,
        rightConnected: Boolean,
        caseConnected: Boolean
    ): SparseArray<ByteArray> {
        val airpodData = ByteArray(27)

        val manufacturerData: SparseArray<ByteArray> = SparseArray(1)
        manufacturerData.append(76, airpodData)
        return manufacturerData
    }

    companion object {

        private const val DEFAULT_MAC_ADDRESS = "12345"
    }
}