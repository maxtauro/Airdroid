package com.maxtauro.airdroid.mainfragment.presenter

import android.bluetooth.le.ScanSettings.SCAN_MODE_LOW_LATENCY
import android.bluetooth.le.ScanSettings.SCAN_MODE_LOW_POWER
import com.hannesdorfmann.mosby3.mvi.MviBasePresenter
import com.jakewharton.rxrelay2.PublishRelay
import com.maxtauro.airdroid.AirpodModel
import com.maxtauro.airdroid.bluetooth.callbacks.AirpodLeScanCallback
import com.maxtauro.airdroid.mIsActivityRunning
import com.maxtauro.airdroid.mainfragment.viewmodel.DeviceFragmentReducer
import com.maxtauro.airdroid.mainfragment.viewmodel.DeviceViewModel
import com.maxtauro.airdroid.utils.BluetoothScannerUtil
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class DeviceStatusPresenter(private val isLocationPermissionEnabled: () -> Boolean) :
    DeviceStatusContract.Presenter,
    MviBasePresenter<DeviceStatusContract.View, DeviceViewModel>() {

    private val reducer: DeviceFragmentReducer = DeviceFragmentReducer(isLocationPermissionEnabled)

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
                DeviceViewModel.createEmptyViewModel(isLocationPermissionEnabled()),
                ::reduce
            )

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
                scannerUtil.startScan(
                    scanCallback = scanCallback,
                    scanMode = if (mIsActivityRunning) {
                        SCAN_MODE_LOW_LATENCY
                    } else {
                        SCAN_MODE_LOW_POWER
                    }
                )
            }
            is UpdateNameIntent -> {
                scannerUtil.startScan(
                    scanCallback = scanCallback,
                    scanMode = if (mIsActivityRunning) {
                        SCAN_MODE_LOW_LATENCY
                    } else {
                        SCAN_MODE_LOW_POWER
                    }
                )
            }
            is ScanForDeviceNameIntent -> {
//                scannerUtil.scanForAirpodName(intent.context)
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