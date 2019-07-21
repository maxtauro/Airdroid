package com.example.airdroid.mainfragment.presenter

import com.example.airdroid.mainfragment.viewmodel.AirpodViewModel
import com.example.airdroid.mainfragment.viewmodel.AirpodViewModelReducer
import com.hannesdorfmann.mosby3.mvi.MviBasePresenter
import io.reactivex.android.schedulers.AndroidSchedulers

class DeviceStatusPresenter : DeviceStatusContract.Presenter,
    MviBasePresenter<DeviceStatusContract.View, AirpodViewModel>() {

    private val reducer: AirpodViewModelReducer = AirpodViewModelReducer()

    override fun bindIntents() {
        val viewModelObservable = intent(DeviceStatusContract.View::actionIntents)
            .observeOn(AndroidSchedulers.mainThread())
            .scan(
                AirpodViewModel.EMPTY,
                reducer::reduce
            )
            .distinctUntilChanged()

        subscribeViewState(viewModelObservable, DeviceStatusContract.View::render)
    }
}