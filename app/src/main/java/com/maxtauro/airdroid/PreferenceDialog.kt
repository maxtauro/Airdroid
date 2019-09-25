package com.maxtauro.airdroid

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context.MODE_PRIVATE
import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.widget.Switch
import androidx.fragment.app.DialogFragment

class PreferenceDialog : DialogFragment() {

    private lateinit var openAppSwitch: Switch
    private lateinit var showNotificationSwitch: Switch

    private var isOpenAppEnabled = true
    private var isNotificationEnabled = true

    private var preferences: SharedPreferences? = null

    private val isSystemAlertWindowPermissionGranted
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Settings.canDrawOverlays(context)
        } else {
            true
        }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        preferences = activity?.getSharedPreferences(SHARED_PREFERENCE_FILE_NAME, MODE_PRIVATE)
            ?: throw IllegalStateException("Preferences haven't been initialized yet")

        isOpenAppEnabled = preferences?.getBoolean(OPEN_APP_PREF_KEY, true) ?: true
        isNotificationEnabled = preferences?.getBoolean(NOTIFICATION_PREF_KEY, true) ?: true

        val builder = AlertDialog.Builder(activity)
            .setTitle("Preferences")
            .setView(createView())
            .setPositiveButton("Save") { _: DialogInterface, i: Int ->
                dismiss()
            }

        val dialog = builder.create()

        // TODO will implement this once the flickering background bug is fixed
//        dialog.window?.attributes?.windowAnimations = R.style.DialogAnimation //style id

        return dialog
    }

    override fun onDismiss(dialog: DialogInterface) {
        preferences?.let {
            val editor = preferences!!.edit()

            editor.putBoolean(OPEN_APP_PREF_KEY, isOpenAppEnabled && isSystemAlertWindowPermissionGranted)
            editor.putBoolean(NOTIFICATION_PREF_KEY, isNotificationEnabled)

            editor.apply()
        }

        super.onDismiss(dialog)
    }

    private fun createView(): View {
        val view = LayoutInflater.from(activity).inflate(R.layout.preference_dialog, null)
        openAppSwitch = view.findViewById(R.id.open_app_switch)
        showNotificationSwitch = view.findViewById(R.id.notification_preference_switch)

        openAppSwitch.setOnCheckedChangeListener { _, isChecked ->
            isOpenAppEnabled = isChecked

            if (isChecked &&
                !isSystemAlertWindowPermissionGranted &&
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
            ) {
                context?.showSystemAlertWindowDialog {
                    openAppSwitch.isChecked = isOpenAppEnabled && isSystemAlertWindowPermissionGranted
                }
            }
        }
        showNotificationSwitch.setOnCheckedChangeListener { _, isChecked ->
            isNotificationEnabled = isChecked
        }

        openAppSwitch.isChecked = isOpenAppEnabled && isSystemAlertWindowPermissionGranted
        showNotificationSwitch.isChecked = isNotificationEnabled

        return view
    }
}