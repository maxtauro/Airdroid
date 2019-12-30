package com.maxtauro.airdroid.deviceactivity

import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import com.maxtauro.airdroid.AirDroidApplication
import com.maxtauro.airdroid.AirpodModel
import com.maxtauro.airdroid.AirpodPiece
import com.maxtauro.airdroid.R

class DeviceStatusActivityView(private val activity: DeviceStatusActivity) {

    private val applicationContext = activity.applicationContext as AirDroidApplication

    fun render() {
        render(applicationContext.mAirpodModel, applicationContext.mAirpodName)
    }

    private fun render(airpodModel: AirpodModel?, airpodName: String? = null) {
        when {
            airpodModel?.isConnected == true -> renderConnected(airpodModel, airpodName)
            airpodModel != null -> renderLoading()
            else -> renderDisconnected()
        }
    }

    private fun renderAirpodName(airpodName: String?) {
        val airpodNameTextView: TextView = activity.findViewById(R.id.text_view_airpod_name)
        airpodNameTextView.text = airpodName
    }

    private fun renderConnected(airpodModel: AirpodModel, airpodName: String? = null) {
        airpodName?.let { renderAirpodName(it) }

        renderLeftPiece(airpodModel.leftAirpod)
        renderCasePiece(airpodModel.case)
        renderRightPiece(airpodModel.rightAirpod)
    }

    private fun renderLoading() {
        renderLoadingLeftPiece()
        renderLoadingCasePiece()
        renderLoadingRightPiece()
    }

    private fun renderDisconnected() {
        renderAirpodName(null)

        renderDisconnectedLeftPiece()
        renderDisconnectedCasePiece()
        renderDisconnectedRightPiece()
    }

    private fun renderLeftPiece(leftAirpod: AirpodPiece) {
        renderPiece(
            layoutId = R.id.left_airpod_piece,
            pieceImageViewId = R.id.left_airpod_piece_img,
            chargeImageViewId = R.id.left_airpod_piece_charge_img,
            chargeTextViewId = R.id.left_airpod_piece_charge_text,
            progressBarId = R.id.left_airpod_progress_bar,
            pieceImageResId = R.drawable.left_pod,
            chargeLevel = leftAirpod.chargeLevel,
            isCharging = leftAirpod.isCharging,
            isConnected = leftAirpod.isConnected
        )
    }

    private fun renderCasePiece(case: AirpodPiece) {
        renderPiece(
            layoutId = R.id.case_airpod_piece,
            pieceImageViewId = R.id.case_airpod_piece_img,
            chargeImageViewId = R.id.case_airpod_piece_charge_img,
            chargeTextViewId = R.id.case_airpod_piece_charge_text,
            progressBarId = R.id.case_progress_bar,
            pieceImageResId = R.drawable.pod_case,
            chargeLevel = case.chargeLevel,
            isCharging = case.isCharging,
            isConnected = case.isConnected
        )
    }

    private fun renderRightPiece(rightAirpod: AirpodPiece) {
        renderPiece(
            layoutId = R.id.right_airpod_piece,
            pieceImageViewId = R.id.right_airpod_piece_img,
            chargeImageViewId = R.id.right_airpod_piece_charge_img,
            chargeTextViewId = R.id.right_airpod_piece_charge_text,
            progressBarId = R.id.right_airpod_progress_bar,
            pieceImageResId = R.drawable.right_pod,
            chargeLevel = rightAirpod.chargeLevel,
            isCharging = rightAirpod.isCharging,
            isConnected = rightAirpod.isConnected
        )
    }

    private fun renderLoadingLeftPiece() {
        renderLoadingPiece(
            layoutId = R.id.left_airpod_piece,
            pieceImageViewId = R.id.left_airpod_piece_img,
            pieceImageResId = R.drawable.left_pod_disconnected,
            progressBarId = R.id.left_airpod_progress_bar,
            chargeImageViewId = R.id.left_airpod_piece_charge_img,
            chargeTextViewId = R.id.left_airpod_piece_charge_text
        )
    }

    private fun renderLoadingCasePiece() {
        renderLoadingPiece(
            layoutId = R.id.case_airpod_piece,
            pieceImageViewId = R.id.case_airpod_piece_img,
            pieceImageResId = R.drawable.pod_case_disconnected,
            progressBarId = R.id.case_progress_bar,
            chargeImageViewId = R.id.case_airpod_piece_charge_img,
            chargeTextViewId = R.id.case_airpod_piece_charge_text,
            showProgressBar = true
        )
    }

    private fun renderLoadingRightPiece() {
        renderLoadingPiece(
            layoutId = R.id.right_airpod_piece,
            pieceImageViewId = R.id.right_airpod_piece_img,
            pieceImageResId = R.drawable.right_pod_disconnected,
            progressBarId = R.id.right_airpod_progress_bar,
            chargeImageViewId = R.id.right_airpod_piece_charge_img,
            chargeTextViewId = R.id.right_airpod_piece_charge_text
        )
    }

    private fun renderDisconnectedLeftPiece() {
        renderDisconnectedPiece(
            layoutId = R.id.left_airpod_piece,
            pieceImageViewId = R.id.left_airpod_piece_img,
            chargeImageViewId = R.id.left_airpod_piece_charge_img,
            chargeTextViewId = R.id.left_airpod_piece_charge_text,
            disconnectedPieceImageResId = R.drawable.left_pod_disconnected
        )
    }

    private fun renderDisconnectedCasePiece() {
        renderDisconnectedPiece(
            layoutId = R.id.case_airpod_piece,
            pieceImageViewId = R.id.case_airpod_piece_img,
            chargeImageViewId = R.id.case_airpod_piece_charge_img,
            chargeTextViewId = R.id.case_airpod_piece_charge_text,
            disconnectedPieceImageResId = R.drawable.pod_case_disconnected
        )
    }

    private fun renderDisconnectedRightPiece() {
        renderDisconnectedPiece(
            layoutId = R.id.right_airpod_piece,
            pieceImageViewId = R.id.right_airpod_piece_img,
            chargeImageViewId = R.id.right_airpod_piece_charge_img,
            chargeTextViewId = R.id.right_airpod_piece_charge_text,
            disconnectedPieceImageResId = R.drawable.right_pod_disconnected
        )
    }

    private fun renderPiece(
        layoutId: Int,
        pieceImageViewId: Int,
        chargeImageViewId: Int,
        chargeTextViewId: Int,
        progressBarId: Int,
        pieceImageResId: Int,
        chargeLevel: Int,
        isCharging: Boolean,
        isConnected: Boolean
    ) {
        val pieceView: LinearLayout = activity.findViewById(layoutId)

        if (isConnected) {
            val pieceImageView: ImageView = activity.findViewById(pieceImageViewId)

            pieceView.visibility = View.VISIBLE
            pieceImageView.setImageResource(pieceImageResId)

            renderChargeImg(
                isCharging = isCharging,
                chargeLevel = chargeLevel,
                chargingImgId = chargeImageViewId,
                chargingTextId = chargeTextViewId,
                progressBarId = progressBarId
            )
        } else pieceView.visibility = View.GONE
    }

    private fun renderLoadingPiece(
        layoutId: Int,
        pieceImageViewId: Int,
        pieceImageResId: Int,
        progressBarId: Int,
        chargeImageViewId: Int,
        chargeTextViewId: Int,
        showProgressBar: Boolean = false
    ) {
        val pieceView: LinearLayout = activity.findViewById(layoutId)
        val pieceImageView: ImageView = activity.findViewById(pieceImageViewId)

        pieceView.visibility = View.VISIBLE
        pieceImageView.setImageResource(pieceImageResId)

        // TODO only show a single progress bar
        renderProgressBar(
            progressBarId = progressBarId,
            chargingImageViewId = chargeImageViewId,
            chargingTextId = chargeTextViewId
        )
    }

    private fun renderDisconnectedPiece(
        layoutId: Int,
        pieceImageViewId: Int,
        chargeImageViewId: Int,
        chargeTextViewId: Int,
        disconnectedPieceImageResId: Int
    ) {
        val pieceView: LinearLayout = activity.findViewById(layoutId)
        val pieceImageView: ImageView = activity.findViewById(pieceImageViewId)
        val chargeImage: ImageView =
            activity.findViewById(chargeImageViewId)
        val chargeText: TextView =
            activity.findViewById(chargeTextViewId)

        pieceView.visibility = View.VISIBLE
        chargeImage.visibility = View.GONE
        chargeText.visibility = View.GONE

        pieceImageView.setImageResource(disconnectedPieceImageResId)
    }

    private fun renderProgressBar(
        progressBarId: Int,
        chargingImageViewId: Int,
        chargingTextId: Int
    ) {
        val progressBar: ProgressBar = activity.findViewById(progressBarId)
        val chargeImageView: ImageView = activity.findViewById(chargingImageViewId)
        val chargeTextView: TextView = activity.findViewById(chargingTextId)

        progressBar.visibility = View.VISIBLE
        chargeImageView.visibility = View.GONE
        chargeTextView.visibility = View.GONE
    }

    private fun renderChargeImg(
        isCharging: Boolean,
        chargeLevel: Int,
        chargingImgId: Int,
        chargingTextId: Int,
        progressBarId: Int
    ) {
        val progressBar: ProgressBar = activity.findViewById(progressBarId)
        val chargeImageView: ImageView = activity.findViewById(chargingImgId)
        val chargeImgTextView: TextView = activity.findViewById(chargingTextId)

        progressBar.visibility = View.GONE
        chargeImageView.visibility = View.VISIBLE
        chargeImgTextView.visibility = View.VISIBLE

        val chargeImgResId: Int =
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

        chargeImageView.setImageResource(chargeImgResId)
        chargeImgTextView.text = "$chargeLevel%"
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
        private const val TAG = "DeviceStatusActivityView"
    }
}