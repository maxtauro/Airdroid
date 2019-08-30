package com.maxtauro.airdroid.mainfragment.presenter

import com.maxtauro.airdroid.AirpodModel
import com.maxtauro.airdroid.bluetooth.callbacks.AirpodLeScanCallback
import com.maxtauro.airdroid.mainfragment.viewmodel.DeviceFragmentReducer
import com.maxtauro.airdroid.mainfragment.viewmodel.DeviceViewModel
import com.maxtauro.airdroid.utils.BluetoothScannerUtil
import com.hannesdorfmann.mosby3.mvi.MviBasePresenter
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class DeviceStatusPresenter : DeviceStatusContract.Presenter,
    MviBasePresenter<DeviceStatusContract.View, DeviceViewModel>() {

    private val reducer: DeviceFragmentReducer = DeviceFragmentReducer()

    private val scannerUtil = BluetoothScannerUtil()
    private val scanCallback = AirpodLeScanCallback(::broadcastScanResult)

    private val eventBus = EventBus.getDefault()
    private val intentsRelay = PublishRelay.create<DeviceStatusIntent>().toSerialized()

    override fun bindIntents() {

        eventBus.register(this)

        val viewModelObservable = Observable.merge(
            intentsRelay,
            intent(DeviceStatusContract.View::actionIntents)
        )
            .observeOn(AndroidSchedulers.mainThread())
            .scan(
                DeviceViewModel.EMPTY,
                ::reduce
            )
            .distinctUntilChanged()

        subscribeViewState(viewModelObservable, DeviceStatusContract.View::render)
    }

    override fun detachView() {
        super.detachView()
        eventBus.unregister(this)
    }

    private fun reduce(viewModel: DeviceViewModel, intent: DeviceStatusIntent): DeviceViewModel {
        preReduce(viewModel, intent)
        return reducer.reduce(viewModel, intent)
    }

    private fun preReduce(viewModel: DeviceViewModel, intent: DeviceStatusIntent) {
        when (intent) {
            is ConnectedIntent -> {
                intentsRelay.accept(InitialScanIntent(intent.deviceName))
                scannerUtil.startScan(scanCallback)
            }
            is UpdateNameIntent -> {
                if (!scannerUtil.isScanning) scannerUtil.startScan(scanCallback)
            }
            is DisconnectedIntent,
            is StopScanIntent -> {
                scannerUtil.stopScan()
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onIntentEvent(intent: DeviceStatusIntent) {
        intentsRelay.accept(intent)
    }

    private fun broadcastScanResult(airpodModel: AirpodModel) {
        if (scannerUtil.isScanning) {
            intentsRelay.accept(RefreshIntent(airpodModel))
        }
    }
}