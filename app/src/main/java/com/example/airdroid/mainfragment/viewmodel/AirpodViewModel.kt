package com.example.airdroid.mainfragment.viewmodel

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class AirpodViewModel private constructor(
    val leftAirpod: AirpodPiece,
    val rightAirpod: AirpodPiece,
    val case: AirpodPiece,
    val lastConnected: Long = System.currentTimeMillis()

) : Parcelable {

    companion object {
        val EMPTY = AirpodViewModel(
            AirpodPiece.EMPTY,
            AirpodPiece.EMPTY,
            AirpodPiece.EMPTY
        )

        // TODO figure out how to parse 5% increments from the manufacturer data
        fun create(manufacturerSpecificData: ByteArray): AirpodViewModel {
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

            return AirpodViewModel(
                AirpodPiece(
                    leftChargeLevel,
                    leftChargingStatus,
                    isLeftConnected
                ),
                AirpodPiece(
                    rightChargeLevel,
                    rightChargingStatus,
                    isRightConnected
                ),
                AirpodPiece(
                    caseChargeLevel,
                    caseChargingStatus,
                    isCaseConnected
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
    val chargingStatus: Boolean,
    val isConnected: Boolean
) : Parcelable {
    companion object {
        val EMPTY = AirpodPiece(
            -1,
            chargingStatus = false,
            isConnected = false
        )
    }
}