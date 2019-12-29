package com.maxtauro.airdroid

import android.os.Bundle
import android.support.wearable.activity.WearableActivity
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class DeviceStatusActivity : WearableActivity() {

    private val eventBus = EventBus.getDefault()

    lateinit var view: DeviceStatusActivityView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_status)

        view = DeviceStatusActivityView(this)
        eventBus.register(this)
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
        view.render()
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
