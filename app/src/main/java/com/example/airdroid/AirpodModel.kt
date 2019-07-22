package com.example.airdroid

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class AirpodModel private constructor(
    val leftAirpod: AirpodPiece,
    val rightAirpod: AirpodPiece,
    val case: AirpodPiece,
    val lastConnected: Long = System.currentTimeMillis()

) : Parcelable {

    val isConnected
        get() = leftAirpod.isConnected || rightAirpod.isConnected || case.isConnected

    companion object {
        val EMPTY = AirpodModel(
            AirpodPiece.LEFT_EMPTY,
            AirpodPiece.RIGHT_EMPTY,
            AirpodPiece.CASE_EMPTY
        )

        // TODO figure out how to parse 5% increments from the manufacturer data
        fun create(manufacturerSpecificData: ByteArray): AirpodModel {
            val decodedHexResult = manufacturerSpecificData.toHexString()

            val leftChargeLevel =
                if (isFlipped(decodedHexResult)) Integer.parseInt(decodedHexResult[12].toString(), 16)
                else Integer.parseInt(decodedHexResult[13].toString(), 16)

            val rightChargeLevel =
                if (isFlipped(decodedHexResult)) Integer.parseInt(decodedHexResult[13].toString(), 16)
                else Integer.parseInt(decodedHexResult[12].toString(), 16)

            val caseChargeLevel = Integer.parseInt(decodedHexResult[15].toString(), 16)

            // charge status
            // - bit 0 = left charging status
            // - bit 1 = right charging status
            // - bit 2 = case charging status)
            val chargeStatus = Integer.parseInt(decodedHexResult[14].toString(), 16)

            val leftChargingStatus = chargeStatus and 1 != 0
            val rightChargingStatus = chargeStatus and 2 != 0
            val caseChargingStatus = chargeStatus and 4 != 0

            val isLeftConnected = leftChargeLevel != 15
            val isRightConnected = rightChargeLevel != 15
            val isCaseConnected = caseChargeLevel != 15

            return AirpodModel(
                AirpodPiece(
                    leftChargeLevel,
                    leftChargingStatus,
                    isLeftConnected,
                    WhichPiece.LEFT
                ),
                AirpodPiece(
                    rightChargeLevel,
                    rightChargingStatus,
                    isRightConnected,
                    WhichPiece.RIGHT
                ),
                AirpodPiece(
                    caseChargeLevel,
                    caseChargingStatus,
                    isCaseConnected,
                    WhichPiece.CASE
                )
            )
        }

        private fun isFlipped(str: String): Boolean {
            return Integer.toString(Integer.parseInt("" + str[10], 16) + 0x10, 2)[3] == '0'
        }

        private fun ByteArray.toHexString() = joinToString("") {
            Integer.toUnsignedString(java.lang.Byte.toUnsignedInt(it), 16).padStart(2, '0').toUpperCase()
        }
    }
}

@Parcelize
data class AirpodPiece(
    val chargeLevel: Int,
    val isCharging: Boolean,
    val isConnected: Boolean,
    val whichPiece: WhichPiece
) : Parcelable {
    companion object {

        val LEFT_EMPTY = AirpodPiece(
            0,
            isCharging = false,
            isConnected = true,
            whichPiece = WhichPiece.LEFT
        )
        val RIGHT_EMPTY = AirpodPiece(
            0,
            isCharging = false,
            isConnected = true,
            whichPiece = WhichPiece.RIGHT
        )
        val CASE_EMPTY = AirpodPiece(
            0,
            isCharging = false,
            isConnected = true,
            whichPiece = WhichPiece.CASE
        )
    }
}

enum class WhichPiece {
    LEFT,
    RIGHT,
    CASE
}