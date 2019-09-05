package com.maxtauro.airdroid.mainfragment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.maxtauro.airdroid.R
import com.maxtauro.airdroid.airpodviews.AirpodPieceView
import com.maxtauro.airdroid.mainfragment.viewmodel.DeviceViewModel

class DeviceFragmentView(
    inflater: LayoutInflater,
    container: ViewGroup?
) {

    val view = inflater.inflate(R.layout.fragment_device, container, false)!!

    private lateinit var titleText: TextView

    private lateinit var leftPieceView: AirpodPieceView
    private lateinit var rightPieceView: AirpodPieceView
    private lateinit var casePieceView: AirpodPieceView

    init {
        bindViews()
    }

    fun render(viewModel: DeviceViewModel) {
        renderDeviceName(viewModel)

        when {
            viewModel.airpods.isConnected -> renderPieces(viewModel)
            viewModel.isInitialScan -> renderInitialScan(viewModel)
            else -> renderDisconnectedView(viewModel)
        }

        renderLocationPermissionMessage(viewModel.shouldShowPermissionsMessage)
    }

    private fun renderDeviceName(viewModel: DeviceViewModel) {
        titleText.text =
            if (viewModel.airpods.isConnected || viewModel.isInitialScan) {
                viewModel.deviceName
            } else {
                view.context.getString(R.string.no_airpods_connected)
            }
    }

    private fun renderInitialScan(viewModel: DeviceViewModel) {
        leftPieceView.renderDisconnected(viewModel.airpods.leftAirpod)
        casePieceView.renderScanning(viewModel.airpods.case)
        rightPieceView.renderDisconnected(viewModel.airpods.rightAirpod)
    }

    private fun renderPieces(viewModel: DeviceViewModel) {
        leftPieceView.render(viewModel.airpods.leftAirpod)
        casePieceView.render(viewModel.airpods.case)
        rightPieceView.render(viewModel.airpods.rightAirpod)
    }

    private fun bindViews() {
        titleText = view.findViewById(R.id.title_text)

        leftPieceView = view.findViewById(R.id.left_airpod_piece)
        casePieceView = view.findViewById(R.id.case_airpod_piece)
        rightPieceView = view.findViewById(R.id.right_airpod_piece)
    }

    private fun renderDisconnectedView(viewModel: DeviceViewModel) {
        leftPieceView.renderDisconnected(viewModel.airpods.leftAirpod)
        casePieceView.renderDisconnected(viewModel.airpods.case)
        rightPieceView.renderDisconnected(viewModel.airpods.rightAirpod)
    }

    private fun renderLocationPermissionMessage(isLocationPermissionEnabled: Boolean) {
        val locationPermissionTextView: TextView =
            view.findViewById(R.id.location_permission_text_view)

        if (isLocationPermissionEnabled) {
            locationPermissionTextView.visibility = View.GONE
        } else {
            locationPermissionTextView.visibility = View.VISIBLE
        }
    }
}