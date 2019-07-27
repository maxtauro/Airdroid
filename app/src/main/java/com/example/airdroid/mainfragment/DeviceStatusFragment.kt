package com.example.airdroid.mainfragment

import android.bluetooth.BluetoothA2dp
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.airdroid.AirpodModel
import com.example.airdroid.EXTRA_DEVICE
import com.example.airdroid.callbacks.AirpodLeScanCallback
import com.example.airdroid.mainfragment.presenter.ConnectedIntent
import com.example.airdroid.mainfragment.presenter.DeviceStatusContract
import com.example.airdroid.mainfragment.presenter.DeviceStatusIntent
import com.example.airdroid.mainfragment.presenter.DeviceStatusPresenter
import com.example.airdroid.mainfragment.presenter.InitialScanIntent
import com.example.airdroid.mainfragment.presenter.RefreshIntent
import com.example.airdroid.mainfragment.viewmodel.DeviceViewModel
import com.example.airdroid.notification.NotificationService
import com.example.airdroid.services.BluetoothConnectionService
import com.example.airdroid.utils.BluetoothScannerUtil
import com.hannesdorfmann.mosby3.mvi.MviFragment
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.disposables.CompositeDisposable

class DeviceStatusFragment :
    MviFragment<DeviceStatusContract.View, DeviceStatusContract.Presenter>(),
    DeviceStatusContract.View {

    val scannerUtil = BluetoothScannerUtil()
    val scanCallback = AirpodLeScanCallback(arrayListOf(), ::broadcastScanResult)

    val subscriptions = CompositeDisposable()

    private lateinit var view: DeviceFragmentView

    private val actionIntentsRelay = PublishRelay.create<DeviceStatusIntent>().toSerialized()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getMvpDelegate().onCreate(savedInstanceState)
    }

    override fun onDestroy() {
        super.onDestroy()
        subscriptions.clear()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = DeviceFragmentView(inflater, container)

        this.view = view
        return view.view
    }

    override fun onResume() {
        super.onResume()

        // Here we check if a head set (ie airpods) is connected to our device
        // Unfortunately there is no way to check is some thing that airpods are connected
        // So we just start the scan if something might be connected

        Handler().postDelayed(
            {
                val connectionState = BluetoothAdapter.getDefaultAdapter().getProfileConnectionState(BluetoothA2dp.HEADSET)
                val deviceName = (activity?.intent?.extras?.get(EXTRA_DEVICE) as? BluetoothDevice)?.name ?: ""
                if (connectionState == 2 || connectionState == 1) {
                    actionIntentsRelay.accept(ConnectedIntent(deviceName))
                }
            },
            1000
        )
    }

    override fun onStop() {
        super.onStop()
        scannerUtil.stopScan()
    }

    override fun actionIntents() = actionIntentsRelay

    override fun createPresenter() = DeviceStatusPresenter()

    override fun render(viewModel: DeviceViewModel) {
        context?.let {
            val notificationService = NotificationService(it, it.packageName)
            notificationService.renderNotification(viewModel)
        }
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

    private fun broadcastScanResult(airpodModel: AirpodModel) {
        actionIntentsRelay.accept(RefreshIntent(airpodModel))
    }

    fun startBluetoothService() {
        Intent(activity, BluetoothConnectionService::class.java).also { intent ->
            activity?.startService(intent)
        }
    }

    companion object {
        private const val TAG = "DeviceStatusFragment"
    }
}