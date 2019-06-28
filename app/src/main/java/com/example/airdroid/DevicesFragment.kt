package com.example.airdroid

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

class DevicesFragment : Fragment(), DevicesContract.View {

    override lateinit var presenter: DevicesContract.Presenter

    override var isActive: Boolean = false
        get() = isAdded

    override fun showBattery() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    companion object {

        fun newInstance() = DevicesFragment()
    }
}