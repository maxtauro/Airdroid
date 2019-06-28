package com.example.airdroid

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.example.airdroid.services.BluetoothConnectionService
import com.example.airdroid.utils.BluetoothUtil
import org.greenrobot.eventbus.EventBus

//TODO rename this (also is the activity the presenter or should that be extracted from the activity?)
class MainActivity : AppCompatActivity() {

    // TODO should this be injected?
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private lateinit var bluetoothUtil: BluetoothUtil

    private val REQUEST_ENABLE_BT = 1000 //TODO move this somewhere else

    private lateinit var devicesActivityPresenter: DevicesPresenter

    private val eventBus = EventBus()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val devicesFragment = supportFragmentManager.findFragmentById(R.id.fragment_devices)
            as DevicesFragment ?: DevicesFragment.newInstance()
        devicesActivityPresenter = DevicesPresenter(devicesFragment, eventBus)
    }

    override fun onStart() {
        super.onStart()

        if (bluetoothAdapter == null) {
            TODO("Handle bluetooth not supported")
        } else if (!bluetoothAdapter.isEnabled) {
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
        startBluetoothService()
        //TODO does anything else need to happen here?
    }

    private fun startBluetoothService() {
        Intent(this, BluetoothConnectionService::class.java).also { intent ->
            startService(intent)
        }
    }

    fun closeWindow(view: View) {
        finish()
    }
}
