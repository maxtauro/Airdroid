package com.example.airdroid

import com.example.airdroid.utils.BluetoothUtil
import org.greenrobot.eventbus.EventBus

class DevicesPresenter(val devicesView: DevicesContract.View, eventBus: EventBus) : DevicesContract.Presenter {

    private lateinit var bluetoothUtil: BluetoothUtil

    init {
        devicesView.presenter = this
    }

    override fun updateBattery() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun start() {
        println("Start from DevicesPresenter")
    }
}