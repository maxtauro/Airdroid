package com.maxtauro.airdroid.mainfragment.presenter

import com.maxtauro.airdroid.mainfragment.viewmodel.DeviceViewModel
import com.hannesdorfmann.mosby3.mvi.MviPresenter
import com.hannesdorfmann.mosby3.mvp.MvpView
import io.reactivex.Observable

interface DeviceStatusContract {

    interface View : MvpView {

        fun actionIntents(): Observable<DeviceStatusIntent>

        fun render(viewModel: DeviceViewModel)
    }

    interface Presenter : MviPresenter<View, DeviceViewModel>
}