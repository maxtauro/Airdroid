package com.example.airdroid

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothProfile
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast

class MainActivity : AppCompatActivity() {

    // TODO should this be injected?
    val bluetoothAdapter: BluetoothAdapter =
        BluetoothAdapter.getDefaultAdapter() ?: TODO("Handle device not having bluetooth compatibility")

    private val REQUEST_ENABLE_BT = 1000 //TODO move this somewhere else

    private var shouldPromptForBluetooth = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Intent(this, BluetoothService::class.java).also { intent ->
            startService(intent)
        }
    }

    override fun onResume() {
        super.onResume()

        if (!bluetoothAdapter.isEnabled && shouldPromptForBluetooth) {
            shouldPromptForBluetooth = false

            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        } else if (bluetoothAdapter.isEnabled) {
            setupBluetooth()
        }
    }

    override fun onStop() {
        super.onStop()
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // TODO, design proper logic for what happens here
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                Toast.makeText(this, "Bluetooth has been enabled", Toast.LENGTH_SHORT).show()
                setupBluetooth()
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(this, "Bluetooth is not enabled", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupBluetooth() {
        val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter.bondedDevices

        if (!areAirpodsPaired(pairedDevices)) {
            TODO("No airpods paired, prompt user for pairing workflow")
            return
        }
    }

    private fun areAirpodsPaired(pairedDevices: Set<BluetoothDevice>?): Boolean {
        // TODO find cleaner way to check if a device is airpod
        return pairedDevices?.any { device -> device.name.equals("Airpods", true) } ?: return false
    }

    fun closeWindow(view: View) {
        finish()
    }
}
