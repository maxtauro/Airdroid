package com.example.airdroid.mainfragment

import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.airdroid.R
import com.example.airdroid.mainfragment.viewmodel.AirpodViewModel

class DeviceFragmentView(
    inflater: LayoutInflater,
    container: ViewGroup?
){

    val view =inflater.inflate(R.layout.fragment_device, container, false)!!

    init {
        bindViews()
    }

    fun render(viewModel: AirpodViewModel) {

    }

    private fun bindViews() {
        //TODO
    }
}