package com.maxtauro.airdroid.deviceactivity

import android.os.Bundle
import android.os.Handler
import android.support.wearable.activity.WearableActivity
import android.util.Log
import com.maxtauro.airdroid.R
import com.maxtauro.airdroid.datalayer.AirpodUpdateEvent
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class DeviceStatusActivity : WearableActivity() {

    private val eventBus = EventBus.getDefault()
    private val mHandler = Handler()

    lateinit var view: DeviceStatusActivityView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_status)

        view = DeviceStatusActivityView(this)
        eventBus.register(this)

        mHandler.postDelayed({ finish() }, 10000)
    }

    override fun onResume() {
        view.render()
        super.onResume()
    }

    override fun onDestroy() {
        eventBus.unregister(this)
        super.onDestroy()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onIntentEvent(intent: AirpodUpdateEvent) {
        Log.d(TAG, "Received AirpodUpdateEvent")
        view.render()
    }

    companion object {
        private const val TAG = "DeviceStatusActivity"
    }
}
