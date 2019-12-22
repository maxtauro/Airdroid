package com.maxtauro.airdroid.DevicePopupActivity.devicepopupfragment.presenter

import android.bluetooth.le.ScanSettings.SCAN_MODE_LOW_LATENCY
import android.util.Log
import com.hannesdorfmann.mosby3.mvi.MviBasePresenter
import com.jakewharton.rxrelay2.PublishRelay
import com.maxtauro.airdroid.AirpodModel
import com.maxtauro.airdroid.DevicePopupActivity.devicepopupfragment.viewmodel.DeviceFragmentReducer
import com.maxtauro.airdroid.DevicePopupActivity.devicepopupfragment.viewmodel.DeviceViewModel
import com.maxtauro.airdroid.bluetooth.AirpodLeScanCallback
import com.maxtauro.airdroid.bluetooth.BluetoothScannerUtil
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class DeviceStatusPresenter(var isLocationPermissionEnabled: () -> Boolean) :
    DeviceStatusContract.Presenter,
    MviBasePresenter<DeviceStatusContract.View, DeviceViewModel>() {

    private var reducer: DeviceFragmentReducer = DeviceFragmentReducer(isLocationPermissionEnabled)

    private val scannerUtil = BluetoothScannerUtil()
    private val scanCallback =
        AirpodLeScanCallback(::broadcastScanResult)

    private val eventBus = EventBus.getDefault()
    private val intentsRelay = PublishRelay.create<DeviceStatusIntent>().toSerialized()

    override fun bindIntents() {

        val viewModelObservable = Observable.merge(
            intentsRelay,
            intent(DeviceStatusContract.View::actionIntents)
        )
            .observeOn(AndroidSchedulers.mainThread())
            .scan(
                DeviceViewModel.createEmptyViewModel(isLocationPermissionEnabled()),
                ::reduce
            )

        subscribeViewState(viewModelObservable, DeviceStatusContract.View::render)
    }

    override fun attachView(view: DeviceStatusContract.View) {
        super.attachView(view)
        eventBus.register(this)
        reducer = DeviceFragmentReducer(view::isLocationPermissionEnabled)
    }

    override fun detachView() {
        super.detachView()
        eventBus.unregister(this)
    }

    private fun reduce(viewModel: DeviceViewModel, intent: DeviceStatusIntent): DeviceViewModel {
        preReduce(viewModel, intent)
        val updatedViewModel = reducer.reduce(viewModel, intent)
        postReduce(updatedViewModel, intent)

        return updatedViewModel
    }

    private fun preReduce(viewModel: DeviceViewModel, intent: DeviceStatusIntent) {
        when (intent) {
            is DisconnectedIntent,
            is StopScanIntent -> scannerUtil.stopScan()
        }
    }

    private fun postReduce(viewModel: DeviceViewModel, intent: DeviceStatusIntent) {
        when (intent) {
            is UpdateFromNotificationIntent,
            is InitialScanIntent,
            is InitialConnectionIntent -> startScan(viewModel)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onIntentEvent(intent: DeviceStatusIntent) {
        intentsRelay.accept(intent)
    }

    private fun startScan(viewModel: DeviceViewModel) {
        scannerUtil.startScan(
            scanCallback = scanCallback,
            scanMode = SCAN_MODE_LOW_LATENCY,
            disableTimeout = viewModel.airpods.isConnected,
            timeoutCallback = { intentsRelay.accept(ScanTimeoutIntent) }
        )
    }

    private fun broadcastScanResult(airpodModel: AirpodModel) {
        if (scannerUtil.isScanning) {
            Log.d(TAG, "broadcastScanResult")
            intentsRelay.accept(RefreshAirpodModelIntent(airpodModel))
        }
    }

    companion object {
        private const val TAG = "DeviceStatusPresenter"
    }
}
