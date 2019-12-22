package com.maxtauro.airdroid.DevicePopupActivity.devicepopupfragment.presenter

import com.hannesdorfmann.mosby3.mvi.MviPresenter
import com.hannesdorfmann.mosby3.mvp.MvpView
import com.maxtauro.airdroid.DevicePopupActivity.devicepopupfragment.viewmodel.DeviceViewModel
import com.maxtauro.airdroid.AirpodModel
import com.maxtauro.airdroid.mainfragment.viewmodel.DeviceViewModel
import io.reactivex.Observable

interface DeviceStatusContract {

    interface View : MvpView {

        fun actionIntents(): Observable<DeviceStatusIntent>

        fun render(viewModel: DeviceViewModel)

        fun isLocationPermissionEnabled(): Boolean

        fun sendWearableUpdate(airpodModel: AirpodModel)
    }

    interface Presenter : MviPresenter<View, DeviceViewModel>
}