package com.maxtauro.airdroid.preferenceactivity


import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.maxtauro.airdroid.*
import com.maxtauro.airdroid.notification.NotificationService
import com.maxtauro.airdroid.notification.NotificationUtil

private const val DIALOG_FRAGMENT_TAG = "NumberPickerDialog"

class PreferenceFragment : PreferenceFragmentCompat() {

    private var openAppSwitchPreference: SwitchPreferenceCompat? = null
    private var notificationSwitchPreference: SwitchPreferenceCompat? = null
    private var darkModeBySettingsSwitchPreference: SwitchPreferenceCompat? = null
    private var darkModeByToggleSwitchPreference: SwitchPreferenceCompat? = null
    private var autoDismissPopupEnabledSwitchPreference: SwitchPreferenceCompat? = null
    private var autoDismissPopupDurationPreference: NumberPickerPreference? = null

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


    override fun onDisplayPreferenceDialog(preference: Preference?) {
        if (parentFragmentManager.findFragmentByTag(DIALOG_FRAGMENT_TAG) != null) {
            return
        }
        if (preference is NumberPickerPreference) {
            val dialog = NumberPickerPreferenceDialog.newInstance(preference.key)
            dialog.setTargetFragment(this, 0)
            dialog.show(parentFragmentManager, DIALOG_FRAGMENT_TAG)
        } else
            super.onDisplayPreferenceDialog(preference)
    }

    private fun setupPreferences() {
        initializePreferences()
        setupPreferenceChangeListeners()
        adjustOpenAppPreferenceForPermission()
    }

    private fun initializePreferences() {
        openAppSwitchPreference =
            findPreference(requireContext().getString(R.string.OPEN_APP_PREF_KEY))
        notificationSwitchPreference =
            findPreference(requireContext().getString(R.string.NOTIFICATION_PREF_KEY))
        autoDismissPopupEnabledSwitchPreference =
            findPreference(requireContext().getString(R.string.AUTO_DISMISS_ENABLED_PREF_KEY))
        autoDismissPopupDurationPreference =
            findPreference(requireContext().getString(R.string.AUTO_DISMISS_DURATION_PREF_KEY))
        darkModeBySettingsSwitchPreference =
            findPreference(requireContext().getString(R.string.DARK_MODE_BY_SYSTEM_SETTINGS_PREF_KEY))
        darkModeByToggleSwitchPreference =
            findPreference(requireContext().getString(R.string.DARK_MODE_BY_TOGGLE_PREF_KEY))

        autoDismissPopupDurationPreference?.isEnabled =
            autoDismissPopupEnabledSwitchPreference?.isChecked == true

        updateDarkModeByToggle(darkModeBySettingsSwitchPreference?.isChecked == true)
    }

    private fun setupPreferenceChangeListeners() {
        openAppSwitchPreference?.setOnPreferenceChangeListener(::onOpenAppSwitchCheckChanged)
        notificationSwitchPreference?.setOnPreferenceChangeListener(::onNotificationSwitchCheckChanged)
        autoDismissPopupEnabledSwitchPreference?.setOnPreferenceChangeListener(::onAutoClosePopupSwitchCheckChanged)
        darkModeBySettingsSwitchPreference?.setOnPreferenceChangeListener(::onDarkModeBySettingsCheckChanged)
        darkModeByToggleSwitchPreference?.setOnPreferenceChangeListener(::onDarkModeByToggleCheckChanged)
    }

    private fun adjustOpenAppPreferenceForPermission() {
        openAppSwitchPreference?.let {
            if (!isSystemAlertWindowPermissionGranted
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
                && it.isChecked
            ) {
                it.performClick()
            }
        }
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
                if (isSystemAlertWindowPermissionGranted) {
                    openAppPreference.setDefaultValue(isChecked && isSystemAlertWindowPermissionGranted)
                } else {
                    openAppPreference.performClick()
                }
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

    private fun onAutoClosePopupSwitchCheckChanged(
        autoClosePreference: Preference,
        isChecked: Any
    ): Boolean {
        autoDismissPopupDurationPreference?.isEnabled = (isChecked as Boolean)
        return true
    }

    private fun onDarkModeBySettingsCheckChanged(
        darkModeBySettingsPreference: Preference,
        isChecked: Any
    ): Boolean {
        updateDarkModeByToggle(isChecked as Boolean)
        onUiModeChanged(
            isDarkModeBySettingsChecked = isChecked,
            isDarkModeByToggleChecked = darkModeByToggleSwitchPreference?.let { it.isChecked }
                .orElse { false })
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
            NotificationUtil.clearNotification(requireContext())
        }
    }

    companion object {
        private const val TAG = "PreferenceFragment"
    }
}