package com.maxtauro.airdroid

import android.app.Activity
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.maxtauro.airdroid.mainfragment.DeviceStatusFragment

var mIsActivityRunning = false

//TODO rename
class MainActivity : AppCompatActivity() {

    private lateinit var deviceStatusFragment: DeviceStatusFragment

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

        setupAds()

        deviceStatusFragment =
            supportFragmentManager.findFragmentById(R.id.fragment_devices) as DeviceStatusFragment
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
            showBluetoothNotSupportedAlertDialog()
        } else if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        } else if (bluetoothAdapter.isEnabled) {
            deviceStatusFragment.startBluetoothService()
        }
    }

    private fun showBluetoothNotSupportedAlertDialog() {
        AlertDialog
            .Builder(this)
            .setTitle("Bluetooth Not Supported")
            .setMessage("It appears that your device is does not support bluetooth")
            .setPositiveButton("Ok") { _, _ ->
                finish()
            }
            .show()
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
    }

    private fun setupAds() {
        MobileAds.initialize(this, getString(R.string.APP_AD_ID))

        val adView = AdView(this)
        adView.adSize = AdSize.BANNER
        adView.adUnitId =
            when {
                BuildConfig.BUILD_TYPE == "release" || true -> getString(R.string.RELEASE_AD_UNIT_ID)
                else -> TEST_AD_UNIT_ID
            }

        addContentView(
            adView, ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
    }

    fun closeWindow(view: View) {
        finish()
    }
}
