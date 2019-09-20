package com.maxtauro.airdroid

import android.os.Build
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class AirpodModel(
    val leftAirpod: AirpodPiece,
    val rightAirpod: AirpodPiece,
    val case: AirpodPiece,
    val lastConnected: Long = System.currentTimeMillis(),
    val macAddress: String = ""

) : Parcelable {

    val isConnected
        get() = leftAirpod.isConnected || rightAirpod.isConnected

    companion object {
        val EMPTY = AirpodModel(
            AirpodPiece.LEFT_EMPTY,
            AirpodPiece.RIGHT_EMPTY,
            AirpodPiece.CASE_EMPTY
        )

        // TODO figure out how to parse 5% increments from the manufacturer data
        fun create(manufacturerSpecificData: ByteArray, address: String): AirpodModel {
            val decodedHexResult = manufacturerSpecificData.toHexString()

            val leftChargeLevel =
                (if (isFlipped(decodedHexResult)) Integer.parseInt(decodedHexResult[12].toString(), 16)
                else Integer.parseInt(decodedHexResult[13].toString(), 16)) * 10

            val rightChargeLevel =
                (if (isFlipped(decodedHexResult)) Integer.parseInt(decodedHexResult[13].toString(), 16)
                else Integer.parseInt(decodedHexResult[12].toString(), 16)) * 10

            val caseChargeLevel = Integer.parseInt(decodedHexResult[15].toString(), 16) * 10

            // charge status
            // - bit 0 = left charging status
            // - bit 1 = right charging status
            // - bit 2 = case charging status)
            val chargeStatus = Integer.parseInt(decodedHexResult[14].toString(), 16)

            val leftChargingStatus = chargeStatus and 1 != 0
            val rightChargingStatus = chargeStatus and 2 != 0
            val caseChargingStatus = chargeStatus and 4 != 0

            val isLeftConnected = leftChargeLevel != 150
            val isRightConnected = rightChargeLevel != 150
            val isCaseConnected = caseChargeLevel != 150

            return AirpodModel(
                AirpodPiece(
                    leftChargeLevel,
                    leftChargingStatus,
                    isLeftConnected,
                    WhichPiece.LEFT,
                    true
                ),
                AirpodPiece(
                    rightChargeLevel,
                    rightChargingStatus,
                    isRightConnected,
                    WhichPiece.RIGHT,
                    true
                ),
                AirpodPiece(
                    caseChargeLevel,
                    caseChargingStatus,
                    isCaseConnected,
                    WhichPiece.CASE,
                    true
                ),
                macAddress = address
            )
        }

        private fun isFlipped(str: String): Boolean {
            return (Integer.parseInt("" + str[10], 16) + 0x10).toString(2)[3] == '0'
        }

        private fun ByteArray.toHexString() =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) joinToString("") {
                Integer.toUnsignedString(java.lang.Byte.toUnsignedInt(it), 16).padStart(2, '0').toUpperCase()
            } else {
                val hexChars = CharArray(this.size * 2)
                for (j in this.indices) {
                    val v = this[j].toInt() and 0xFF // Here is the conversion
                    hexChars[j * 2] = hexCharset[v.ushr(4)]
                    hexChars[j * 2 + 1] = hexCharset[v and 0x0F]
                }
                String(hexChars)
            }


        private val hexCharset = charArrayOf(
            '0',
            '1',
            '2',
            '3',
            '4',
            '5',
            '6',
            '7',
            '8',
            '9',
            'A',
            'B',
            'C',
            'D',
            'E',
            'F'
        )

    }
}

@Parcelize
data class AirpodPiece(
    val chargeLevel: Int,
    val isCharging: Boolean,
    val isConnected: Boolean,
    val whichPiece: WhichPiece,
    val shouldShowBatteryInfo: Boolean
) : Parcelable {
    companion object {

        val LEFT_EMPTY = AirpodPiece(
            0,
            isCharging = false,
            isConnected = false,
            whichPiece = WhichPiece.LEFT,
            shouldShowBatteryInfo = false
        )
        val RIGHT_EMPTY = AirpodPiece(
            0,
            isCharging = false,
            isConnected = false,
            whichPiece = WhichPiece.RIGHT,
            shouldShowBatteryInfo = false
        )
        val CASE_EMPTY = AirpodPiece(
            0,
            isCharging = false,
            isConnected = false,
            whichPiece = WhichPiece.CASE,
            shouldShowBatteryInfo = false
        )
    }
}

enum class WhichPiece {
    LEFT,
    RIGHT,
    CASE
}