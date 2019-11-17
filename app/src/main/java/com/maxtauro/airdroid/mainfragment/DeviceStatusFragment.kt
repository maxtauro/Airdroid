package com.maxtauro.airdroid.mainfragment

import android.bluetooth.BluetoothA2dp
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothProfile.STATE_CONNECTED
import android.bluetooth.BluetoothProfile.STATE_CONNECTING
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.crashlytics.android.Crashlytics
import com.hannesdorfmann.mosby3.mvi.MviFragment
import com.jakewharton.rxrelay2.PublishRelay
import com.maxtauro.airdroid.AirpodModel
import com.maxtauro.airdroid.mainfragment.presenter.*
import com.maxtauro.airdroid.mainfragment.viewmodel.DeviceViewModel
import com.maxtauro.airdroid.notification.NotificationService
import com.maxtauro.airdroid.notification.NotificationUtil
import com.maxtauro.airdroid.notification.NotificationUtil.Companion.EXTRA_AIRPOD_MODEL
import com.maxtauro.airdroid.orElse
import io.reactivex.disposables.CompositeDisposable

class DeviceStatusFragment :
    MviFragment<DeviceStatusContract.View, DeviceStatusContract.Presenter>(),
    DeviceStatusContract.View {

    var refreshingUiMode: Boolean = false

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
        Crashlytics.log(Log.DEBUG, TAG, ".onResume")

        // Whenever the app comes into the foreground we clear the notification
        context?.let {
            Crashlytics.log(Log.DEBUG, TAG, " stopping notification service from onResume")
            stopNotificationService(it)
        }

        val startFlag: StartFlag? = getExtra<StartFlag>(EXTRA_START_FLAG)
        Crashlytics.log(Log.DEBUG, TAG, "StartFlag = ${startFlag?.name}")

        when (startFlag) {
            StartFlag.AIRPODS_CONNECTED -> onAirPodsConnectedStartFlag()
            StartFlag.NOTIFICATION_CLICKED -> onNotificationClickedStartFlag()
            else -> onNoStartFlagResume()
        }

        if (!refreshingUiMode) clearOnResumeFlag()
    }

    override fun onPause() {
        super.onPause()

        actionIntentsRelay.accept(StopScanIntent)
        if (viewModel.airpods.isConnected ||
            connectionState == 2 ||
            connectionState == 1
        ) {
            if (!refreshingUiMode) {
                context?.let {
                    Crashlytics.log(Log.DEBUG, TAG, " starting notification service from onStop")
                    startNotificationService(it)
                }
            }
        }

        refreshingUiMode = false
    }

    fun onRefreshUiMode() {
        refreshingUiMode = true
    }

    override fun actionIntents() = actionIntentsRelay

    override fun createPresenter() = DeviceStatusPresenter(::isLocationPermissionEnabled)

    override fun render(viewModel: DeviceViewModel) {

        if (viewModel.shouldShowTimeoutToast) {
            Toast.makeText(
                this.context,
                "Could not determine AirPod battery status",
                Toast.LENGTH_LONG
            ).show()

            actionIntentsRelay.accept(ScanTimeoutToastShownIntent)
        }

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

    private fun onNoStartFlagResume() {
        // Here we check if a head set (ie airpods) is connected to our device
        // Unfortunately there is no way to check is some thing that airpods are connected
        // So we just start the scan if something might be connected
        if (connectionState == STATE_CONNECTED || connectionState == STATE_CONNECTING) {
            actionIntentsRelay.accept(InitialScanIntent)
        } else {
            // When we resume the activity and nothing is connected, we need to
            // explicitly post the DisconnectedIntent or else the previous viewModel will just
            // be rendered again (since Mosby persists the viewModel).
            actionIntentsRelay.accept(DisconnectedIntent)
        }
    }

    private fun onNotificationClickedStartFlag() {
        getExtra<AirpodModel>(EXTRA_AIRPOD_MODEL)?.let {
            actionIntentsRelay.accept(UpdateFromNotificationIntent(it))
        }.orElse {
            when (connectionState) {
                STATE_CONNECTED,
                STATE_CONNECTING -> actionIntentsRelay.accept(StartScanIntent)
                else -> actionIntentsRelay.accept(DisconnectedIntent)
            }
        }
    }

    private fun onAirPodsConnectedStartFlag() {
        actionIntentsRelay.accept(InitialConnectionIntent)
    }

    private fun startNotificationService(context: Context) {
        Intent(context, NotificationService::class.java).also { intent ->
            intent.putExtra(EXTRA_AIRPOD_MODEL, viewModel.airpods)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }

    private fun stopNotificationService(context: Context) {
        Crashlytics.log(Log.DEBUG, TAG, " stopping notification service")

        Intent(activity, NotificationService::class.java).also { intent ->
            activity?.stopService(intent)
        }
        NotificationUtil.clearNotification(context)
        (0..1).last
    }

    private fun clearOnResumeFlag() = activity?.intent?.removeExtra(EXTRA_START_FLAG)

    @Suppress("UNCHECKED_CAST")
    private fun <T> getExtra(key: String): T? {
        return activity?.intent?.extras?.get(key) as? T
    }

    companion object {
        private const val TAG = "DeviceStatusFragment"

        const val EXTRA_START_FLAG = "EXTRA_START_FLAG"

        enum class StartFlag {
            AIRPODS_CONNECTED,
            NOTIFICATION_CLICKED,
            DARK_MODE_TOGGLED,
            NONE
        }
    }
}