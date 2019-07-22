package com.example.airdroid

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

class AirpodPieceView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private lateinit var airpodPieceImg: ImageView
    private lateinit var airpodPieceChargeImg: ImageView
    private lateinit var airpodPieceChargeText: TextView

    init {
        addView(View.inflate(context, R.layout.airpod_piece_view_layout, null))
        bindViews()
    }

    fun render(airpodPiece: AirpodPiece) {
        if (airpodPiece.isConnected) {
            renderImg(airpodPiece.whichPiece)
            renderChargingAttributes(airpodPiece.isCharging, airpodPiece.chargeLevel)
        } else {
            airpodPieceImg.visibility = View.GONE
            airpodPieceChargeImg.visibility = View.GONE
            airpodPieceChargeText.visibility = View.GONE
        }
    }

    fun renderDisconnected(airpodPiece: AirpodPiece) {
        val resId = when (airpodPiece.whichPiece) {
            WhichPiece.LEFT -> R.drawable.left_pod_disconnected
            WhichPiece.RIGHT -> R.drawable.right_pod_disconnected
            WhichPiece.CASE -> R.drawable.pod_case_disconnected
        }
        airpodPieceImg.setImageResource(resId)

        airpodPieceChargeImg.visibility = View.INVISIBLE
        airpodPieceChargeText.visibility = View.INVISIBLE
    }

    private fun bindViews() {
        airpodPieceImg = findViewById(R.id.airpod_piece_img)
        airpodPieceChargeImg = findViewById(R.id.airpod_piece_charge_img)
        airpodPieceChargeText = findViewById(R.id.airpod_piece_charge_text)
    }

    private fun renderChargingAttributes(isCharging: Boolean, chargeLevel: Int) {
        renderChargeImg(isCharging, chargeLevel)
        renderChargeTxt(chargeLevel)
    }

    private fun renderChargeTxt(chargeLevel: Int) {
        airpodPieceChargeText.text = "$chargeLevel%" //resources.getString(R.string.charge_text, chargeLevel)
    }

    private fun renderChargeImg(isCharging: Boolean, chargeLevel: Int) {
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
        airpodPieceChargeImg.setImageResource(resId)
    }

    private fun renderImg(whichPiece: WhichPiece) {
        val resId = when (whichPiece) {
            WhichPiece.LEFT -> R.drawable.left_pod
            WhichPiece.RIGHT -> R.drawable.right_pod
            WhichPiece.CASE -> R.drawable.pod_case
        }
        airpodPieceImg.setImageResource(resId)
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
}