package com.maxtauro.airdroid

import android.app.Activity
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.maxtauro.airdroid.mainfragment.DeviceStatusFragment
import com.maxtauro.airdroid.mainfragment.presenter.ReRenderIntent

var mIsActivityRunning = false

class MainActivity : AppCompatActivity() {

    private lateinit var deviceStatusFragment: DeviceStatusFragment

    lateinit var settingsButton: FloatingActionButton

    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

    private val isLocationPermissionEnabled
        get() = ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupAds()

        deviceStatusFragment =
            supportFragmentManager.findFragmentById(R.id.fragment_devices) as DeviceStatusFragment

        settingsButton = findViewById(R.id.settings_btn)
        settingsButton.setOnClickListener {
            PreferenceDialog().show(supportFragmentManager, "PreferenceDialog")
        }
    }

    override fun onStart() {
        super.onStart()

        if (!isLocationPermissionEnabled) {
            showLocationPermissionDialog()
        }

        if (bluetoothAdapter == null) {
            showBluetoothNotSupportedAlertDialog()
        } else if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        } else if (bluetoothAdapter.isEnabled) {
            deviceStatusFragment.startBluetoothService()
        }
    }

    // TODO, find more elegant way to check this
    override fun onResume() {
        super.onResume()
        mIsActivityRunning = true
    }

    override fun onPause() {
        super.onPause()
        mIsActivityRunning = false
    }

    override fun onStop() {
        super.onStop()
        mIsActivityRunning = false
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                Toast.makeText(this, "Bluetooth has been enabled", Toast.LENGTH_SHORT).show()
                deviceStatusFragment.startBluetoothService()
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(this, "Bluetooth is not enabled", Toast.LENGTH_SHORT).show()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_ENABLE_COARSE_LOCATION) {
            if (grantResults.firstOrNull() != PackageManager.PERMISSION_GRANTED &&
                !ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                showLocationPermissionDialog()
            }
            deviceStatusFragment.actionIntents().accept(ReRenderIntent)
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun setupAds() {
        MobileAds.initialize(this, getString(R.string.APP_AD_ID))

        val adView: AdView = findViewById(R.id.adView)

        val adRequest =
            if (BuildConfig.BUILD_TYPE == "release") {
                AdRequest.Builder().build()
            } else {
                AdRequest.Builder().addTestDevice("652EBD92D970E40C0A6C7619AE8FA570").build()
            }
        adView.loadAd(adRequest)
    }


    private fun showBluetoothNotSupportedAlertDialog() {
        AlertDialog
            .Builder(this)
            .setTitle(getString(R.string.bluetooth_not_supported))
            .setMessage(getString(R.string.device_does_not_support_bluetooth))
            .setPositiveButton(getString(R.string.positive_btn_label)) { _, _ ->
                finish()
            }
            .show()
    }

    private fun showLocationPermissionDialog() {

        AlertDialog.Builder(this)
            .setMessage(getString(R.string.location_permission_explanation_message))
            .setPositiveButton(getString(R.string.positive_btn_label)) { _: DialogInterface, _: Int ->
                requestLocationPermission()
            }
            .setNegativeButton(getString(R.string.deny_btn_label)) { _: DialogInterface, _: Int ->
                finish()
            }
            .setCancelable(false)
            .create()
            .show()
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
            REQUEST_ENABLE_COARSE_LOCATION
        )
    }

    fun closeWindow(view: View) {
        finish()
    }

    companion object {
        private const val REQUEST_ENABLE_BT = 1000
        private const val REQUEST_ENABLE_COARSE_LOCATION = 1001
    }
}
