package com.maxtauro.airdroid.DevicePopupActivity

import android.app.Activity
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.codemybrainsout.ratingdialog.RatingDialog
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.maxtauro.airdroid.*
import com.maxtauro.airdroid.DevicePopupActivity.devicepopupfragment.DevicePopupFragment
import com.maxtauro.airdroid.DevicePopupActivity.devicepopupfragment.presenter.ReRenderIntent
import com.maxtauro.airdroid.preferenceactivity.PreferenceActivity

var mIsActivityRunning = false

class DevicePopupActivity : AppCompatActivity() {

    private lateinit var deviceStatusFragment: DevicePopupFragment

    private lateinit var settingsButton: FloatingActionButton
    private lateinit var preferences: SharedPreferences

    private val shouldShowSystemAlertWindowDialog: Boolean
        get() = preferences.getBoolean(SHOULD_SHOW_SYSTEM_ALERT_PROMPT_KEY, true)

    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

    private val isLocationPermissionEnabled
        get() = ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    private val isSystemAlertWindowPermissionGranted
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Settings.canDrawOverlays(this)
        } else {
            true
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_popup)

        preferences = PreferenceManager.getDefaultSharedPreferences(this)
        migratePreferences()

        deviceStatusFragment =
            supportFragmentManager.findFragmentById(R.id.fragment_devices) as DevicePopupFragment

        settingsButton = findViewById(R.id.settings_btn)
        settingsButton.setOnClickListener { startPreferenceActivity() }

        configureNightMode()

        if (!deviceStatusFragment.refreshingUiMode) {
            setupRatingDialog()
            setupAds()
        }

        rebindDialog()
    }

    override fun onStart() {
        super.onStart()

        if (!isLocationPermissionEnabled) {
            showLocationPermissionDialog()
        } else requestSystemAlertWindowPermission()

        if (bluetoothAdapter == null) {
            showBluetoothNotSupportedAlertDialog()
        } else if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(
                enableBtIntent,
                REQUEST_ENABLE_BT
            )
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
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                Toast.makeText(this, "Bluetooth has been enabled", Toast.LENGTH_SHORT).show()
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
            } else requestSystemAlertWindowPermission()

            deviceStatusFragment.actionIntents().accept(ReRenderIntent)
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun setupAds() {
        // I know it'd be very easy to build my app and remove the ad
        // please don't remove it, it's a super unintrusive ad that gives
        // me a very very small amount of revenue for an app that
        // I've worked hard to give away for free
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

    /**
     * This method is used to migrate from an old shared preferences file to a new shared preferences file after updating the app
     * This is needed because we are now using the AndroidX Preference library to handle out preferences
     */
    private fun migratePreferences() {
        val oldPreferences = getSharedPreferences(
            SHARED_PREFERENCE_FILE_NAME,
            Context.MODE_PRIVATE
        )
            ?: throw IllegalStateException("Preferences haven't been initialized yet")

        val hasMigratedPreferences =
            preferences.getBoolean(HAS_MIGRATED_PREF_KEY, false)

        if (!hasMigratedPreferences) {

            val oldNotificationPref = oldPreferences.getBoolean(NOTIFICATION_PREF_KEY, true)
            val oldOpenAppPref = oldPreferences.getBoolean(OPEN_APP_PREF_KEY, true)
            val oldShouldShowSystemAlertPref =
                oldPreferences.getBoolean(SHOULD_SHOW_SYSTEM_ALERT_PROMPT_KEY, true)
            val oldDarkModeBySystemPref =
                oldPreferences.getBoolean(DARK_MODE_BY_TOGGLE_PREF_KEY, true)
            val oldDarkModeByTogglePref = oldPreferences.getBoolean(NOTIFICATION_PREF_KEY, false)

            val editor = preferences.edit()

            editor.putBoolean(NOTIFICATION_PREF_KEY, oldNotificationPref)
            editor.putBoolean(OPEN_APP_PREF_KEY, oldOpenAppPref)
            editor.putBoolean(SHOULD_SHOW_SYSTEM_ALERT_PROMPT_KEY, oldShouldShowSystemAlertPref)
            editor.putBoolean(DARK_MODE_BY_TOGGLE_PREF_KEY, oldDarkModeBySystemPref)
            editor.putBoolean(NOTIFICATION_PREF_KEY, oldDarkModeByTogglePref)
            editor.putBoolean(HAS_MIGRATED_PREF_KEY, true)

            editor.apply()
        }
    }

    private fun showBluetoothNotSupportedAlertDialog() {
        AlertDialog
            .Builder(this)
            .setTitle(getString(R.string.bluetooth_not_supported))
            .setMessage(getString(R.string.device_does_not_support_bluetooth))
            .setPositiveButton(getString(R.string.positive_btn_label)) { _, _ ->
                if (BuildConfig.BUILD_TYPE == "release") finish()
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

    private fun requestSystemAlertWindowPermission() {
        if (!isSystemAlertWindowPermissionGranted &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
            shouldShowSystemAlertWindowDialog
        ) {
            showSystemAlertWindowDialog()
            preferences.edit().putBoolean(SHOULD_SHOW_SYSTEM_ALERT_PROMPT_KEY, false)
                .apply()
        }
    }

    private fun startPreferenceActivity() {
        Intent(this, PreferenceActivity::class.java).also { intent ->
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            this.startActivity(intent)
        }
    }

    private fun refreshUiMode() {
        val defaultNightMode = getDefaultNightMode()

        deviceStatusFragment.onRefreshUiMode()
        AppCompatDelegate.setDefaultNightMode(defaultNightMode)
    }

    private fun configureNightMode() {
        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        val defaultNightMode = getDefaultNightMode()

        Log.d(TAG, "current Night mode $currentNightMode, default: $defaultNightMode")

        val willChangeUiMode =
            (currentNightMode == Configuration.UI_MODE_NIGHT_YES &&
                    defaultNightMode == AppCompatDelegate.MODE_NIGHT_NO) ||
                    (currentNightMode == Configuration.UI_MODE_NIGHT_NO &&
                            defaultNightMode == AppCompatDelegate.MODE_NIGHT_YES)

        if (willChangeUiMode) {
            Log.d(TAG, "First if")
            deviceStatusFragment.refreshingUiMode = true
            AppCompatDelegate.setDefaultNightMode(defaultNightMode)
        } else {
            deviceStatusFragment.refreshingUiMode = false
        }
    }

    private fun getDefaultNightMode(): Int {
        val isDarkModeBySettingsEnabled =
            preferences.getBoolean(DARK_MODE_BY_SYSTEM_SETTINGS_PREF_KEY, true)

        val isDarkModeByToggleEnabled =
            preferences.getBoolean(DARK_MODE_BY_TOGGLE_PREF_KEY, true)

        return when {
            isDarkModeBySettingsEnabled -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                } else {
                    AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY
                }
            }
            isDarkModeByToggleEnabled -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_NO
        }
    }

    private fun setupRatingDialog() {

        val ratingDialog = RatingDialog.Builder(this)
            .session(10)
            .threshold(3f)
            .title(getString(R.string.rating_dialog_title))
            .positiveButtonText(getString(R.string.rating_positive_button_text))
            .negativeButtonText(getString(R.string.rating_negative_button_text))
            .onRatingBarFormSumbit {
                // TODO send an email instead
                val msg = "User feedback: $it"
                FirebaseCrashlytics.getInstance().recordException(
                    UserFeedbackException(
                        msg
                    )
                )
            }.build()

        ratingDialog.show()
    }

    private fun rebindDialog() {
        (supportFragmentManager.findFragmentByTag(PREFERENCE_DIALOG_TAG) as? PreferenceDialog)?.apply {
            this.onUiModeChanged = this@DevicePopupActivity::refreshUiMode
        }
    }

    fun closeWindow(view: View) {
        finish()
    }

    companion object {
        private const val TAG = "DevicePopupActivity"

        private const val PREFERENCE_DIALOG_TAG = "PreferenceDialog.TAG"

        private const val REQUEST_ENABLE_BT = 1000
        private const val REQUEST_ENABLE_COARSE_LOCATION = 1001
        private const val REQUEST_ENABLE_SYSTEM_ALERT_WINDOW = 1002
    }
}
