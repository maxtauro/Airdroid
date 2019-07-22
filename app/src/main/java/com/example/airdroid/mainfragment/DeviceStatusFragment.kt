package com.example.airdroid.mainfragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.airdroid.mainfragment.presenter.DeviceStatusContract
import com.example.airdroid.mainfragment.presenter.DeviceStatusIntent
import com.example.airdroid.mainfragment.presenter.DeviceStatusPresenter
import com.example.airdroid.mainfragment.viewmodel.DeviceViewModel
import com.hannesdorfmann.mosby3.MviDelegateCallback
import com.hannesdorfmann.mosby3.mvi.MviFragment
import com.jakewharton.rxrelay2.PublishRelay

class DeviceStatusFragment :
    MviFragment<DeviceStatusContract.View, DeviceStatusContract.Presenter>(),
    DeviceStatusContract.View
//    MviDelegateCallback<DeviceStatusContract.View, DeviceStatusContract.Presenter>
{

    private lateinit var view: DeviceFragmentView

    private val actionIntentsRelay = PublishRelay.create<DeviceStatusIntent>().toSerialized()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getMvpDelegate().onCreate(savedInstanceState)
        startScanForConnectedAirpods()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = DeviceFragmentView(inflater, container)

        this.view = view
        return view.view
    }

    override fun actionIntents() = actionIntentsRelay

    override fun createPresenter() = DeviceStatusPresenter()

    override fun render(viewModel: DeviceViewModel) {
        view.render(viewModel)
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

    private fun startScanForConnectedAirpods() {
        // TODO, on creation of the fragment, if device that looks like airpods are connected, we'll scan for the info
    }

    companion object {

        private const val TAG = "DeviceStatusFragment"
    }
}