package com.example.airdroid

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.airdroid.services.BluetoothConnectionService

//TODO rename
class MainActivity : AppCompatActivity() {

    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

    private val REQUEST_ENABLE_BT = 1000 //TODO move this somewhere else
    private val REQUEST_ENABLE_COARSE_LOCATION = 1001

    private val isLocationPermissionEnabled
        get() = ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onStart() {
        super.onStart()

        if (!isLocationPermissionEnabled) {
            //TODO show explanatory dialog as to why we need this permission
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_ENABLE_COARSE_LOCATION
            )
        }

        if (bluetoothAdapter == null) {
            TODO("Handle bluetooth not supported")
        } else if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        } else if (bluetoothAdapter.isEnabled) {
            startBluetoothService()
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
                startBluetoothService()
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(this, "Bluetooth is not enabled", Toast.LENGTH_SHORT).show()
            }
        }
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
