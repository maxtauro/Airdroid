package com.maxtauro.airdroid.preferenceactivity

import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.preference.PreferenceManager
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.maxtauro.airdroid.BuildConfig
import com.maxtauro.airdroid.DARK_MODE_BY_SYSTEM_SETTINGS_PREF_KEY
import com.maxtauro.airdroid.DARK_MODE_BY_TOGGLE_PREF_KEY
import com.maxtauro.airdroid.R

class PreferenceActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preferences)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings, PreferenceFragment())
            .commit()

        setupToolbar()
        setupAds()
    }

    override fun onBackPressed() {
        finish()
    }

    fun onUiModeChanged(
        isDarkModeBySettingsChecked: Boolean?,
        isDarkModeByToggleChecked: Boolean?
    ) {
        val defaultNightMode =
            getDefaultNightMode(isDarkModeBySettingsChecked, isDarkModeByToggleChecked)
        AppCompatDelegate.setDefaultNightMode(defaultNightMode)
    }

    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.preference_activity_toolbar)
        setSupportActionBar(findViewById(R.id.preference_activity_toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupAds() {
        val adView: AdView = findViewById(R.id.adView)
        adView.loadAd(AdRequest.Builder().build())
    }

    private fun getDefaultNightMode(
        darkModeBySettingsChecked: Boolean?,
        darkModeByToggleChecked: Boolean?
    ): Int {
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)

        val isDarkModeBySettingsEnabled =
            darkModeBySettingsChecked ?: preferences.getBoolean(
                DARK_MODE_BY_SYSTEM_SETTINGS_PREF_KEY,
                true
            )

        val isDarkModeByToggleEnabled =
            darkModeByToggleChecked ?: preferences.getBoolean(DARK_MODE_BY_TOGGLE_PREF_KEY, true)

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
}