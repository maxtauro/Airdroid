package com.example.airdroid.mainfragment

import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.airdroid.AirpodPieceView
import com.example.airdroid.R
import com.example.airdroid.mainfragment.viewmodel.DeviceViewModel

class DeviceFragmentView(
    inflater: LayoutInflater,
    container: ViewGroup?
) {

    val view = inflater.inflate(R.layout.fragment_device, container, false)!!

    private lateinit var leftPieceView: AirpodPieceView
    private lateinit var rightPieceView: AirpodPieceView
    private lateinit var casePieceView: AirpodPieceView

    init {
        bindViews()
    }

    fun render(viewModel: DeviceViewModel) {
        if (viewModel.airpods.isConnected) renderPieces(viewModel)
        else renderDisconnectedView(viewModel)
    }

    private fun renderPieces(viewModel: DeviceViewModel) {
        leftPieceView.render(viewModel.airpods.leftAirpod)
        casePieceView.render(viewModel.airpods.case)
        rightPieceView.render(viewModel.airpods.rightAirpod)
    }

    private fun bindViews() {
        leftPieceView = view.findViewById(R.id.left_airpod_piece)
        casePieceView = view.findViewById(R.id.case_airpod_piece)
        rightPieceView = view.findViewById(R.id.right_airpod_piece)
    }

    private fun renderDisconnectedView(viewModel: DeviceViewModel) {
        leftPieceView.renderDisconnected(viewModel.airpods.leftAirpod)
        casePieceView.renderDisconnected(viewModel.airpods.case)
        rightPieceView.renderDisconnected(viewModel.airpods.rightAirpod)
    }
}