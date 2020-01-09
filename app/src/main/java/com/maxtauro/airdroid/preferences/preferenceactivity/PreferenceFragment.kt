package com.maxtauro.airdroid.preferences.preferenceactivity

import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.maxtauro.airdroid.R
import com.maxtauro.airdroid.customtap.MediaSessionService
import com.maxtauro.airdroid.isHeadsetConnected
import com.maxtauro.airdroid.notification.NotificationService
import com.maxtauro.airdroid.notification.NotificationUtil
import com.maxtauro.airdroid.orElse
import com.maxtauro.airdroid.preferences.preferenceutils.PreferenceKeys
import com.maxtauro.airdroid.showSystemAlertWindowDialog

class PreferenceFragment : PreferenceFragmentCompat() {

    private var preferences: SharedPreferences? = null

    private var openAppSwitchPreference: SwitchPreferenceCompat? = null
    private var notificationSwitchPreference: SwitchPreferenceCompat? = null
    private var darkModeBySettingsSwitchPreference: SwitchPreferenceCompat? = null
    private var darkModeByToggleSwitchPreference: SwitchPreferenceCompat? = null
    private var enableCustomTapSwitchPreference: SwitchPreferenceCompat? = null

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
        openAppSwitchPreference = findPreference(PreferenceKeys.OPEN_APP_PREF_KEY.key)
        notificationSwitchPreference = findPreference(PreferenceKeys.NOTIFICATION_PREF_KEY.key)
        darkModeBySettingsSwitchPreference =
            findPreference(PreferenceKeys.DARK_MODE_BY_SYSTEM_SETTINGS_PREF_KEY.key)
        darkModeByToggleSwitchPreference =
            findPreference(PreferenceKeys.DARK_MODE_BY_TOGGLE_PREF_KEY.key)
        enableCustomTapSwitchPreference =
            findPreference(PreferenceKeys.ENABLE_CUSTOM_TAP_PREF_KEY.key)

        updateDarkModeByToggle(darkModeBySettingsSwitchPreference?.isChecked == true)
    }

    private fun setupPreferenceChangeListeners() {
        openAppSwitchPreference?.setOnPreferenceChangeListener(::onOpenAppSwitchCheckChanged)
        notificationSwitchPreference?.setOnPreferenceChangeListener(::onNotificationSwitchCheckChanged)
        darkModeBySettingsSwitchPreference?.setOnPreferenceChangeListener(::onDarkModeBySettingsCheckChanged)
        darkModeByToggleSwitchPreference?.setOnPreferenceChangeListener(::onDarkModeByToggleCheckChanged)
        enableCustomTapSwitchPreference?.setOnPreferenceChangeListener(::onEnableCustomTaCheckChanged)
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

    private fun onNotificationSwitchCheckChanged(
        notificationPreference: Preference,
        isChecked: Any
    ): Boolean {
        if (isChecked as Boolean && isHeadsetConnected) startNotificationService()
        else stopNotificationService()

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

    private fun onDarkModeByToggleCheckChanged(
        darkModeBySettingsPreference: Preference,
        isChecked: Any
    ): Boolean {
        onUiModeChanged(isDarkModeByToggleChecked = isChecked as Boolean)
        return true
    }

    private fun onEnableCustomTaCheckChanged(
        enableCustomTapPreference: Preference,
        isChecked: Any
    ): Boolean {
        if (isChecked as Boolean) startMediaSessionService()
        else stopMediaSessionService()

        return true
    }

    private fun updateDarkModeByToggle(isChecked: Boolean) {
        val shouldDisableToggleSwitch =
            isChecked && darkModeByToggleSwitchPreference?.let { it.isChecked }.orElse { false }

        if (shouldDisableToggleSwitch) darkModeByToggleSwitchPreference?.performClick()
        darkModeByToggleSwitchPreference?.isEnabled = !isChecked
    }


    private fun onUiModeChanged(
        isDarkModeBySettingsChecked: Boolean? = null,
        isDarkModeByToggleChecked: Boolean? = null
    ) {
        (activity as PreferenceActivity).onUiModeChanged(
            isDarkModeBySettingsChecked,
            isDarkModeByToggleChecked
        )
    }

    private fun startNotificationService() {
        context?.let {
            Intent(context, NotificationService::class.java).also { intent ->

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context?.startForegroundService(intent)
                } else {
                    context?.startService(intent)
                }
            }
        }
    }

    private fun stopNotificationService() {
        context?.let {
            Log.d(TAG, " stopping notification service")

            Intent(activity, NotificationService::class.java).also { intent ->
                activity?.stopService(intent)
            }
            NotificationUtil.clearNotification(context!!)
        }
    }

    private fun startMediaSessionService() {
        Intent(context, MediaSessionService::class.java).also {
            context?.startService(it)
        }
    }

    private fun stopMediaSessionService() {
        Intent(context, MediaSessionService::class.java).also {
            context?.stopService(it)
        }
    }

    companion object {
        private const val TAG = "PreferenceFragment"
    }
}