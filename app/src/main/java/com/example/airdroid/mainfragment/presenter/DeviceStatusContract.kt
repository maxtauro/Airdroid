package com.example.airdroid.mainfragment.presenter

import com.example.airdroid.mainfragment.viewmodel.AirpodViewModel
import com.hannesdorfmann.mosby3.mvi.MviPresenter
import com.hannesdorfmann.mosby3.mvp.MvpView
import io.reactivex.Observable

interface DeviceStatusContract {

    interface View : MvpView {

        fun actionIntents(): Observable<DeviceStatusIntent>

        fun render(viewModel: AirpodViewModel)
    }

    interface Presenter : MviPresenter<View, AirpodViewModel>
}