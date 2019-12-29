package com.maxtauro.airdroid

import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView

class DeviceStatusActivityView(private val activity: DeviceStatusActivity) {

    private val applicationContext = activity.applicationContext as AirDroidApplication

    init {
        render()
    }

    fun render() {
        render(applicationContext.mAirpodModel, applicationContext.mAirpodName)
    }

    fun render(airpodModel: AirpodModel?, airpodName: String? = null) {
        if (airpodModel != null && airpodModel.isConnected) renderConnected(airpodModel, airpodName)
        else renderDisconnected()
    }

    private fun renderConnected(airpodModel: AirpodModel, airpodName: String? = null) {
        airpodName?.let { renderAirpodName(it) }

        renderLeftPiece(airpodModel.leftAirpod)
        renderCasePiece(airpodModel.case)
        renderRightPiece(airpodModel.rightAirpod)
    }

    private fun renderAirpodName(airpodName: String?) {
        val airpodNameTextView: TextView = activity.findViewById(R.id.text_view_airpod_name)
        airpodNameTextView.text = airpodName

    }

    private fun renderDisconnected() {
        renderDisconnectedLeftPiece()
        renderDisconnectedCasePiece()
        renderDisconnectedRightPiece()
    }

    private fun renderLeftPiece(leftAirpod: AirpodPiece) {
        val leftPieceView: LinearLayout = activity.findViewById(R.id.left_airpod_piece)

        if (leftAirpod.isConnected) {
            val leftPieceImageView: ImageView = activity.findViewById(R.id.left_airpod_piece_img)

            leftPieceView.visibility = View.VISIBLE
            leftPieceImageView.setImageResource(R.drawable.left_pod)

            renderChargeImg(
                leftAirpod.isCharging,
                leftAirpod.chargeLevel,
                R.id.left_airpod_piece_charge_img,
                R.id.left_airpod_piece_charge_text,
                R.id.left_airpod_progress_bar
            )
        } else leftPieceView.visibility = View.GONE
    }

    private fun renderCasePiece(case: AirpodPiece) {
        val casePieceView: LinearLayout = activity.findViewById(R.id.case_airpod_piece)

        if (case.isConnected) {
            val casePieceImageView: ImageView = activity.findViewById(R.id.case_airpod_piece_img)

            casePieceView.visibility = View.VISIBLE
            casePieceImageView.setImageResource(R.drawable.pod_case)

            renderChargeImg(
                case.isCharging,
                case.chargeLevel,
                R.id.case_airpod_piece_charge_img,
                R.id.case_airpod_piece_charge_text,
                R.id.case_progress_bar
            )
        } else casePieceView.visibility = View.GONE
    }

    private fun renderRightPiece(rightAirpod: AirpodPiece) {
        val rightPieceView: LinearLayout = activity.findViewById(R.id.right_airpod_piece)

        if (rightAirpod.isConnected) {
            val rightPieceImageView: ImageView = activity.findViewById(R.id.right_airpod_piece_img)

            rightPieceView.visibility = View.VISIBLE
            rightPieceImageView.setImageResource(R.drawable.right_pod)

            renderChargeImg(
                rightAirpod.isCharging,
                rightAirpod.chargeLevel,
                R.id.right_airpod_piece_charge_img,
                R.id.right_airpod_piece_charge_text,
                R.id.right_airpod_progress_bar
            )
        } else rightPieceView.visibility = View.GONE
    }

    private fun renderDisconnectedLeftPiece() {
        val leftPieceView: LinearLayout = activity.findViewById(R.id.left_airpod_piece)
        val leftPieceImageView: ImageView = activity.findViewById(R.id.left_airpod_piece_img)
        val leftPieceChargeImage: ImageView =
            activity.findViewById(R.id.left_airpod_piece_charge_img)
        val leftPieceChargeText: TextView =
            activity.findViewById(R.id.left_airpod_piece_charge_text)

        leftPieceView.visibility = View.VISIBLE
        leftPieceChargeImage.visibility = View.GONE
        leftPieceChargeText.visibility = View.GONE

        leftPieceImageView.setImageResource(R.drawable.left_pod_disconnected)
    }

    private fun renderDisconnectedCasePiece() {
        val casePieceView: LinearLayout = activity.findViewById(R.id.case_airpod_piece)
        val casePieceImageView: ImageView = activity.findViewById(R.id.case_airpod_piece_img)
        val casePieceChargeImage: ImageView =
            activity.findViewById(R.id.case_airpod_piece_charge_img)
        val casePieceChargeText: TextView =
            activity.findViewById(R.id.case_airpod_piece_charge_text)

        casePieceView.visibility = View.VISIBLE
        casePieceChargeImage.visibility = View.GONE
        casePieceChargeText.visibility = View.GONE

        casePieceImageView.setImageResource(R.drawable.pod_case_disconnected)
    }

    private fun renderDisconnectedRightPiece() {
        val rightPieceView: LinearLayout = activity.findViewById(R.id.right_airpod_piece)
        val rightPieceImageView: ImageView = activity.findViewById(R.id.right_airpod_piece_img)
        val rightPieceChargeImage: ImageView =
            activity.findViewById(R.id.right_airpod_piece_charge_img)
        val rightPieceChargeText: TextView =
            activity.findViewById(R.id.right_airpod_piece_charge_text)

        rightPieceView.visibility = View.VISIBLE
        rightPieceChargeImage.visibility = View.GONE
        rightPieceChargeText.visibility = View.GONE

        rightPieceImageView.setImageResource(R.drawable.right_pod_disconnected)
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