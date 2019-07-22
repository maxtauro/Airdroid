package com.example.airdroid.mainfragment.presenter

import com.example.airdroid.mainfragment.viewmodel.DeviceFragmentReducer
import com.example.airdroid.mainfragment.viewmodel.DeviceViewModel
import com.hannesdorfmann.mosby3.mvi.MviBasePresenter
import io.reactivex.android.schedulers.AndroidSchedulers

class DeviceStatusPresenter : DeviceStatusContract.Presenter,
    MviBasePresenter<DeviceStatusContract.View, DeviceViewModel>() {

    private val reducer: DeviceFragmentReducer = DeviceFragmentReducer()

    override fun bindIntents() {
        val viewModelObservable = intent(DeviceStatusContract.View::actionIntents)
            .observeOn(AndroidSchedulers.mainThread())
            .scan(
                DeviceViewModel.EMPTY,
                reducer::reduce
            )
            .distinctUntilChanged()

        subscribeViewState(viewModelObservable, DeviceStatusContract.View::render)
    }
}