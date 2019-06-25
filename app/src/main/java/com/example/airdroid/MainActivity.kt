package com.example.airdroid

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View

class MainActivity : AppCompatActivity() {


    private val REQUEST_ENABLE_BT = 1000 //TODO move this somewhere else

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()

        val bluetoothAdapter: BluetoothAdapter =
            BluetoothAdapter.getDefaultAdapter() ?: TODO("Handle device not having bluetooth compatibility")

        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        }

        val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter.bondedDevices


        if (!areAirpodsPaired(pairedDevices)) {
            TODO("No airpods paired, prompt user for pairing workflow")
        }



        val stop = "hammer time"
    }


    private fun areAirpodsPaired(pairedDevices: Set<BluetoothDevice>?): Boolean {
        // TODO find cleaner way to check if a device is airpod
        return pairedDevices?.any { device -> device.name.equals("Airpods", true) } ?: return false
    }

    fun closeWindow(view: View) {
        finish()
    }
}
