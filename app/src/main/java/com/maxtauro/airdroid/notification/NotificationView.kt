package com.maxtauro.airdroid.notification

import android.view.View
import android.widget.RemoteViews
import com.maxtauro.airdroid.AirpodModel
import com.maxtauro.airdroid.AirpodPiece
import com.maxtauro.airdroid.R
import com.maxtauro.airdroid.WhichPiece

class NotificationView(
    private val isProEnabled: () -> Boolean,
    packageName: String,
    isLargeNotification: Boolean
) :
    RemoteViews(
        packageName,
        if (isLargeNotification) {
            R.layout.notification_large
        } else {
            R.layout.notification_small
        }
    ) {

    // For now, since the notification view is so similar to the fragment view,
    // we will use the same model for both, if they start getting different we will separate them out
    fun render(airpods: AirpodModel) {
        if (airpods.isConnected) renderConnected(airpods)
        else renderDisconnected()
    }

    private fun renderDisconnected() {
        renderLoadingLeftPiece()
        renderLoadingCasePiece()
        renderLoadingRightPiece()
    }

    private fun renderConnected(airpods: AirpodModel) {
        renderLeftPiece(airpods.leftAirpod)
        renderCase(airpods.case)
        renderRightPiece(airpods.rightAirpod)
    }

    private fun renderLeftPiece(leftAirpod: AirpodPiece) {
        if (leftAirpod.isConnected) {
            val podImgResId = getImgResId(WhichPiece.LEFT, true)

            setViewVisibility(R.id.left_airpod_piece, View.VISIBLE)
            setImageViewResource(R.id.left_airpod_piece_img, podImgResId)
            renderChargeImg(
                leftAirpod.isCharging,
                leftAirpod.chargeLevel,
                R.id.left_airpod_piece_charge_img,
                R.id.left_airpod_piece_charge_text,
                R.id.left_airpod_progress_bar
            )
        } else setViewVisibility(R.id.left_airpod_piece, View.GONE)
    }

    private fun renderCase(case: AirpodPiece) {
        if (case.isConnected) {
            setViewVisibility(R.id.case_airpod_piece, View.VISIBLE)
            setImageViewResource(R.id.case_airpod_piece_img, R.drawable.pod_case)
            renderChargeImg(
                case.isCharging,
                case.chargeLevel,
                R.id.case_airpod_piece_charge_img,
                R.id.case_airpod_piece_charge_text,
                R.id.case_progress_bar
            )
        } else setViewVisibility(R.id.case_airpod_piece, View.GONE)
    }

    private fun renderRightPiece(rightAirpod: AirpodPiece) {
        if (rightAirpod.isConnected) {
            val podImgResId = getImgResId(WhichPiece.RIGHT, true)

            setViewVisibility(R.id.right_airpod_piece, View.VISIBLE)
            setImageViewResource(R.id.right_airpod_piece_img, podImgResId)
            renderChargeImg(
                rightAirpod.isCharging,
                rightAirpod.chargeLevel,
                R.id.right_airpod_piece_charge_img,
                R.id.right_airpod_piece_charge_text,
                R.id.right_airpod_progress_bar
            )
        } else setViewVisibility(R.id.right_airpod_piece, View.GONE)
    }

    private fun renderLoadingLeftPiece() {
        val podImgResId = getImgResId(WhichPiece.LEFT, false)

        setViewVisibility(R.id.left_airpod_piece, View.VISIBLE)
        setImageViewResource(R.id.left_airpod_piece_img, podImgResId)
        renderProgressBar(
            R.id.left_airpod_progress_bar,
            R.id.left_airpod_piece_charge_img,
            R.id.left_airpod_piece_charge_text
        )
    }

    private fun renderLoadingCasePiece() {
        setViewVisibility(R.id.case_airpod_piece, View.VISIBLE)
        setImageViewResource(R.id.case_airpod_piece_img, R.drawable.pod_case_disconnected)
        renderProgressBar(
            R.id.case_progress_bar,
            R.id.case_airpod_piece_charge_img,
            R.id.case_airpod_piece_charge_text
        )
    }

    private fun renderLoadingRightPiece() {
        val podImgResId = getImgResId(WhichPiece.RIGHT, false)

        setViewVisibility(R.id.right_airpod_piece, View.VISIBLE)
        setImageViewResource(R.id.right_airpod_piece_img, podImgResId)
        renderProgressBar(
            R.id.right_airpod_progress_bar,
            R.id.right_airpod_piece_charge_img,
            R.id.right_airpod_piece_charge_text
        )
    }

    private fun renderProgressBar(
        progressBarId: Int,
        chargingImgId: Int,
        chargingTextId: Int
    ) {
        setViewVisibility(progressBarId, View.VISIBLE)
        setViewVisibility(chargingImgId, View.GONE)
        setViewVisibility(chargingTextId, View.GONE)
    }

    private fun renderChargeImg(
        isCharging: Boolean,
        chargeLevel: Int,
        chargingImgId: Int,
        chargingTextId: Int,
        progressBarId: Int
    ) {

        setViewVisibility(progressBarId, View.GONE)
        setViewVisibility(chargingImgId, View.VISIBLE)
        setViewVisibility(chargingTextId, View.VISIBLE)

        val resId: Int =
            if (isCharging) {
                when (chargeLevel) {
                    in 0..20 -> BatteryImgResId.BATTERY_CHARGING_20.resId
                    in 21..30 -> BatteryImgResId.BATTERY_CHARGING_30.resId
                    in 31..50 -> BatteryImgResId.BATTERY_CHARGING_50.resId
                    in 51..60 -> BatteryImgResId.BATTERY_CHARGING_60.resId
                    in 61..80 -> BatteryImgResId.BATTERY_CHARGING_80.resId
                    in 81..90 -> BatteryImgResId.BATTERY_CHARGING_90.resId
                    else -> BatteryImgResId.BATTERY_CHARGING_FULL.resId
                }
            } else {
                when (chargeLevel) {
                    in 0..20 -> BatteryImgResId.BATTERY_20.resId
                    in 21..30 -> BatteryImgResId.BATTERY_30.resId
                    in 31..50 -> BatteryImgResId.BATTERY_50.resId
                    in 51..60 -> BatteryImgResId.BATTERY_60.resId
                    in 61..80 -> BatteryImgResId.BATTERY_80.resId
                    in 81..90 -> BatteryImgResId.BATTERY_90.resId
                    else -> BatteryImgResId.BATTERY_FULL.resId

                }
            }
        setImageViewResource(chargingImgId, resId)
        setTextViewText(chargingTextId, "$chargeLevel%")
    }

    private fun getImgResId(whichPiece: WhichPiece, isConnected: Boolean): Int {
        return if (isProEnabled()) getProImgResID(whichPiece, isConnected)
        else getStandardPodImgResId(whichPiece, isConnected)
    }

    private fun getStandardPodImgResId(whichPiece: WhichPiece, isConnected: Boolean): Int {
        return if (isConnected) {
            when (whichPiece) {
                WhichPiece.LEFT -> com.maxtauro.airdroidcommon.R.drawable.left_pod
                WhichPiece.RIGHT -> com.maxtauro.airdroidcommon.R.drawable.right_pod
                WhichPiece.CASE -> com.maxtauro.airdroidcommon.R.drawable.pod_case
            }
        } else {
            when (whichPiece) {
                WhichPiece.LEFT -> com.maxtauro.airdroidcommon.R.drawable.left_pod_disconnected
                WhichPiece.RIGHT -> com.maxtauro.airdroidcommon.R.drawable.right_pod_disconnected
                WhichPiece.CASE -> com.maxtauro.airdroidcommon.R.drawable.pod_case_disconnected
            }
        }
    }

    private fun getProImgResID(whichPiece: WhichPiece, isConnected: Boolean): Int {
        return if (isConnected) {
            when (whichPiece) {
                WhichPiece.LEFT -> com.maxtauro.airdroidcommon.R.drawable.left_pod_pro
                WhichPiece.RIGHT -> com.maxtauro.airdroidcommon.R.drawable.right_pod_pro
                WhichPiece.CASE -> com.maxtauro.airdroidcommon.R.drawable.pod_case
            }
        } else {
            when (whichPiece) {
                WhichPiece.LEFT -> com.maxtauro.airdroidcommon.R.drawable.left_pod_pro_disconnected
                WhichPiece.RIGHT -> com.maxtauro.airdroidcommon.R.drawable.right_pod_pro_disconnected
                WhichPiece.CASE -> com.maxtauro.airdroidcommon.R.drawable.pod_case_disconnected
            }
        }
    }

    private enum class BatteryImgResId(val resId: Int) {
        BATTERY_20(R.drawable.battery_20),
        BATTERY_30(R.drawable.battery_30),
        BATTERY_50(R.drawable.battery_50),
        BATTERY_60(R.drawable.battery_60),
        BATTERY_80(R.drawable.battery_80),
        BATTERY_90(R.drawable.battery_90),
        BATTERY_FULL(R.drawable.battery_full),
        BATTERY_CHARGING_20(R.drawable.battery_charging_20),
        BATTERY_CHARGING_30(R.drawable.battery_charging_30),
        BATTERY_CHARGING_50(R.drawable.battery_charging_50),
        BATTERY_CHARGING_60(R.drawable.battery_charging_60),
        BATTERY_CHARGING_80(R.drawable.battery_charging_80),
        BATTERY_CHARGING_90(R.drawable.battery_charging_90),
        BATTERY_CHARGING_FULL(R.drawable.battery_charging_full)
    }

    companion object {
        const val TAG = "NotificationView"
    }
}