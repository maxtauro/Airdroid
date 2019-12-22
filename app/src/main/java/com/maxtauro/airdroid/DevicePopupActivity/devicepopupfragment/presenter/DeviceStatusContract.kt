package com.maxtauro.airdroid.DevicePopupActivity.devicepopupfragment.presenter

import com.hannesdorfmann.mosby3.mvi.MviPresenter
import com.hannesdorfmann.mosby3.mvp.MvpView
import com.maxtauro.airdroid.DevicePopupActivity.devicepopupfragment.viewmodel.DeviceViewModel
import io.reactivex.Observable

interface DeviceStatusContract {

    interface View : MvpView {

        fun actionIntents(): Observable<DeviceStatusIntent>

        fun render(viewModel: DeviceViewModel)

        fun isLocationPermissionEnabled(): Boolean
    }

    interface Presenter : MviPresenter<View, DeviceViewModel>
}