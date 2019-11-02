package com.maxtauro.airdroid.mainfragment

import android.bluetooth.BluetoothA2dp
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.hannesdorfmann.mosby3.mvi.MviFragment
import com.jakewharton.rxrelay2.PublishRelay
import com.maxtauro.airdroid.*
import com.maxtauro.airdroid.bluetooth.services.BluetoothConnectionService
import com.maxtauro.airdroid.mainfragment.presenter.*
import com.maxtauro.airdroid.mainfragment.viewmodel.DeviceViewModel
import com.maxtauro.airdroid.notification.NotificationService
import com.maxtauro.airdroid.notification.NotificationUtil
import io.reactivex.disposables.CompositeDisposable

class DeviceStatusFragment :
    MviFragment<DeviceStatusContract.View, DeviceStatusContract.Presenter>(),
    DeviceStatusContract.View {

    private val subscriptions = CompositeDisposable()

    private lateinit var view: DeviceFragmentView
    private var viewModel = DeviceViewModel.createEmptyViewModel(isLocationPermissionEnabled())

    private val connectionState: Int?
        get() = BluetoothAdapter.getDefaultAdapter()?.getProfileConnectionState(BluetoothA2dp.HEADSET)

    private val actionIntentsRelay = PublishRelay.create<DeviceStatusIntent>().toSerialized()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getMvpDelegate().onCreate(savedInstanceState)
    }

    override fun onDestroy() {
        super.onDestroy()
        subscriptions.clear()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = DeviceFragmentView(inflater, container)

        this.view = view
        return view.view
    }

    override fun onResume() {
        super.onResume()

        Intent(context, BluetoothConnectionService::class.java).also { intent ->
            context?.startService(intent)
        }

        // Whenever the app comes into the foreground we clear the notification
        context?.let { stopNotificationService(it) }

        // Here we check if a head set (ie airpods) is connected to our device
        // Unfortunately there is no way to check is some thing that airpods are connected
        // So we just start the scan if something might be connected
        if (connectionState == 2 || connectionState == 1) {
            mConnectedDevice?.let {
                actionIntentsRelay.accept(UpdateNameIntent(it.name))
            }.orElse {
                val deviceName =
                    (activity?.intent?.extras?.get(EXTRA_DEVICE) as? BluetoothDevice)?.name
                        ?: ""
                actionIntentsRelay.accept(ConnectedIntent(deviceName))
            }

            (activity?.intent?.extras?.get(NotificationUtil.EXTRA_AIRPOD_MODEL) as? AirpodModel)?.let {
                actionIntentsRelay.accept(RefreshIntent(it))
            }

        } else {
            // When we resume the activity and nothing is connected, we need to
            // explicitly post the DisconnectedIntent or else the previous viewModel will just
            // be rendered again (since Mosby persists the viewModel).
            actionIntentsRelay.accept(DisconnectedIntent)
        }

    }

    override fun onPause() {
        super.onPause()
        actionIntentsRelay.accept(StopScanIntent)
        if (viewModel.airpods.isConnected ||
            connectionState == 2 ||
            connectionState == 1
        ) {
            context?.let { startNotificationService(it) }
        }

        Intent(context, BluetoothConnectionService::class.java).also { intent ->
            context?.stopService(intent)
        }
    }

    override fun actionIntents() = actionIntentsRelay

    override fun createPresenter() = DeviceStatusPresenter(::isLocationPermissionEnabled)

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

    override fun isLocationPermissionEnabled() =
        context != null && ContextCompat.checkSelfPermission(
            context!!,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    fun startBluetoothService() {
        Intent(activity, BluetoothConnectionService::class.java).also { intent ->
            activity?.startServiceIfDeviceUnlocked(intent)
        }
    }

    private fun startNotificationService(context: Context) {
        Intent(context, NotificationService::class.java).also { intent ->
            intent.putExtra(NotificationUtil.EXTRA_AIRPOD_MODEL, viewModel.airpods)
            context.startService(intent)
        }
    }

    private fun stopNotificationService(context: Context) {
        Intent(activity, NotificationService::class.java).also { intent ->
            activity?.stopService(intent)
        }
        NotificationUtil.clearNotification(context)
    }

    companion object {
        private const val TAG = "DeviceStatusFragment"
    }
}