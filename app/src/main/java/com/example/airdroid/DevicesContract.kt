package com.example.airdroid

interface DevicesContract {

    interface View: BaseView<Presenter> {

        var isActive: Boolean

        fun showBattery()
    }

    interface Presenter: BasePresenter {

        fun updateBattery()

    }

}