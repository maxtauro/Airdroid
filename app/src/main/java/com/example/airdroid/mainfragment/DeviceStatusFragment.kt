package com.example.airdroid.mainfragment

import android.util.Log
import com.example.airdroid.mainfragment.presenter.DeviceStatusContract
import com.example.airdroid.mainfragment.presenter.DeviceStatusIntent
import com.example.airdroid.mainfragment.presenter.DeviceStatusPresenter
import com.example.airdroid.mainfragment.viewmodel.AirpodViewModel
import com.hannesdorfmann.mosby3.MviDelegateCallback
import com.jakewharton.rxrelay2.PublishRelay

class DeviceStatusFragment : DeviceStatusContract.View,
    MviDelegateCallback<DeviceStatusContract.View, DeviceStatusContract.Presenter> {

    private lateinit var view: DeviceFragmentView

    private var isRestoringViewState = false

    private val actionIntentsRelay = PublishRelay.create<DeviceStatusIntent>().toSerialized()

    override fun actionIntents() = actionIntentsRelay

    override fun createPresenter() = DeviceStatusPresenter()

    override fun render(viewModel: AirpodViewModel) {
        view.render(viewModel)
    }

    override fun setRestoringViewState(restoringViewState: Boolean) {
        this.isRestoringViewState = restoringViewState
    }

    override fun getMvpView(): DeviceStatusContract.View {
        try {
            return this
        } catch (e: ClassCastException) {
            val msg = "Couldn't cast the View to the corresponding View interface."
            Log.e(TAG, msg)
            throw RuntimeException(msg, e)
        }
    }

    companion object {

        private const val TAG = "DeviceStatusFragment"
    }
}