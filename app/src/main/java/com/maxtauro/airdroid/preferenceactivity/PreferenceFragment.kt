package com.maxtauro.airdroid.preferenceactivity

//    private var isOpenAppEnabled = true
//    private var isNotificationEnabled = true
//    private var isDarkModeBySettingsEnabled = true

import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.maxtauro.airdroid.*

class PreferenceFragment : PreferenceFragmentCompat() {

    private var preferences: SharedPreferences? = null

    private var openAppSwitchPreference: SwitchPreferenceCompat? = null
    private var notificationSwitchPreference: SwitchPreferenceCompat? = null
    private var darkModeBySettingsSwitchPreference: SwitchPreferenceCompat? = null
    private var darkModeByToggleSwitchPreference: SwitchPreferenceCompat? = null

    private val isSystemAlertWindowPermissionGranted
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Settings.canDrawOverlays(context)
        } else {
            true
        }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
        setupPreferences()
    }

    private fun setupPreferences() {
        initializePreferences()
        setupPreferenceChangeListeners()
    }

    private fun initializePreferences() {
        openAppSwitchPreference = findPreference(OPEN_APP_PREF_KEY)
        notificationSwitchPreference = findPreference(NOTIFICATION_PREF_KEY)
        darkModeBySettingsSwitchPreference = findPreference(DARK_MODE_BY_SYSTEM_SETTINGS_PREF_KEY)
        darkModeByToggleSwitchPreference = findPreference(DARK_MODE_BY_TOGGLE_PREF_KEY)

        updateDarkModeByToggle(darkModeBySettingsSwitchPreference?.isChecked == true)
    }

    private fun setupPreferenceChangeListeners() {
        openAppSwitchPreference?.setOnPreferenceChangeListener(::onOpenAppSwitchCheckChanged)
        darkModeBySettingsSwitchPreference?.setOnPreferenceChangeListener(::onDarkModeBySettingsCheckChanged)
        darkModeByToggleSwitchPreference?.setOnPreferenceChangeListener(::onDarkModeByToggleCheckChanged)
    }

    private fun onOpenAppSwitchCheckChanged(
        openAppPreference: Preference,
        isChecked: Any
    ): Boolean {
        isChecked as Boolean

        if (isChecked &&
            !isSystemAlertWindowPermissionGranted &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
        ) {
            context?.showSystemAlertWindowDialog {
                openAppPreference.setDefaultValue(isChecked && isSystemAlertWindowPermissionGranted)
            }
        }
        return true
    }

    private fun onDarkModeBySettingsCheckChanged(
        darkModeBySettingsPreference: Preference,
        isChecked: Any
    ): Boolean {
        updateDarkModeByToggle(isChecked as Boolean)
        onUiModeChanged()
        return true
    }

    private fun updateDarkModeByToggle(isChecked: Boolean) {
        val shouldDisableToggleSwitch =
            isChecked && darkModeByToggleSwitchPreference?.let { it.isChecked }.orElse { false }

        if (shouldDisableToggleSwitch) darkModeByToggleSwitchPreference?.performClick()
        darkModeByToggleSwitchPreference?.isEnabled = !isChecked
    }

    private fun onDarkModeByToggleCheckChanged(
        darkModeBySettingsPreference: Preference,
        isChecked: Any
    ): Boolean {
        onUiModeChanged(isDarkModeByToggleChecked = isChecked as Boolean)
        return true
    }

    private fun onUiModeChanged(
        isDarkModeBySettingsChecked: Boolean? = null,
        isDarkModeByToggleChecked: Boolean? = null
    ) {
        (activity as PreferenceActivity).onUiModeChanged(isDarkModeBySettingsChecked, isDarkModeByToggleChecked)
    }


    companion object {
        private const val TAG = "PreferenceFragment"
    }
}