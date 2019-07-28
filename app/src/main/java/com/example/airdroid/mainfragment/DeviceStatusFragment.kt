package com.example.airdroid.mainfragment

import android.bluetooth.BluetoothA2dp
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.airdroid.EXTRA_DEVICE
import com.example.airdroid.bluetooth.services.BluetoothConnectionService
import com.example.airdroid.mainfragment.presenter.ConnectedIntent
import com.example.airdroid.mainfragment.presenter.DeviceStatusContract
import com.example.airdroid.mainfragment.presenter.DeviceStatusIntent
import com.example.airdroid.mainfragment.presenter.DeviceStatusPresenter
import com.example.airdroid.mainfragment.presenter.InitialScanIntent
import com.example.airdroid.mainfragment.presenter.StopScanIntent
import com.example.airdroid.mainfragment.presenter.UpdateNameIntent
import com.example.airdroid.mainfragment.viewmodel.DeviceViewModel
import com.example.airdroid.notification.NotificationService
import com.example.airdroid.notification.NotificationService.Companion.EXTRA_AIRPOD_MODEL
import com.example.airdroid.notification.NotificationService.Companion.EXTRA_AIRPOD_NAME
import com.example.airdroid.orElse
import com.hannesdorfmann.mosby3.mvi.MviFragment
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class DeviceStatusFragment :
    MviFragment<DeviceStatusContract.View, DeviceStatusContract.Presenter>(),
    DeviceStatusContract.View {

    private val subscriptions = CompositeDisposable()

    private lateinit var view: DeviceFragmentView
    private var viewModel = DeviceViewModel.EMPTY

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

        // When ever the app comes into the foreground we clear the notification
        context?.let { stopNotificationService(it) }

        // Here we check if a head set (ie airpods) is connected to our device
        // Unfortunately there is no way to check is some thing that airpods are connected
        // So we just start the scan if something might be connected
        Handler().postDelayed(
            {
                val connectionState =
                    BluetoothAdapter.getDefaultAdapter().getProfileConnectionState(BluetoothA2dp.HEADSET)

                if (connectionState == 2 || connectionState == 1) {

                    activity?.intent?.getStringExtra(EXTRA_AIRPOD_NAME)?.let {
                        actionIntentsRelay.accept(UpdateNameIntent(it))
                    }.orElse {
                        val deviceName =  (activity?.intent?.extras?.get(EXTRA_DEVICE) as? BluetoothDevice)?.name ?: ""
                        actionIntentsRelay.accept(ConnectedIntent(deviceName))
                    }
                }
            },
            1000
        )
    }

    override fun onPause() {
        super.onPause()
        actionIntentsRelay.accept(StopScanIntent)
        if (viewModel.airpods.isConnected) {
            startNotificationService()
        }
    }

    override fun actionIntents() = actionIntentsRelay

    override fun createPresenter() = DeviceStatusPresenter()

    override fun render(viewModel: DeviceViewModel) {
        this.viewModel = viewModel
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

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(EXTRA_AIRPOD_NAME, viewModel.deviceName)
        super.onSaveInstanceState(outState)
    }

    fun startBluetoothService() {
        Intent(activity, BluetoothConnectionService::class.java).also { intent ->
            activity?.startService(intent)
        }
    }

    private fun startNotificationService() {
        Intent(activity, NotificationService::class.java).also { intent ->
            intent.putExtra(EXTRA_AIRPOD_MODEL, viewModel.airpods)
            intent.putExtra(EXTRA_AIRPOD_NAME, viewModel.deviceName)
            activity?.startService(intent)
        }
    }

    private fun stopNotificationService(context: Context) = GlobalScope.launch(Dispatchers.Main) {
        NotificationService.clearNotification(context)
        Intent(activity, NotificationService::class.java).also { intent ->
            activity?.stopService(intent)
        }
    }

    companion object {
        private const val TAG = "DeviceStatusFragment"
    }
}