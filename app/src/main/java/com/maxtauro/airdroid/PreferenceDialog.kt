package com.maxtauro.airdroid

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context.MODE_PRIVATE
import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Bundle
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
        return builder.create()
    }

    override fun onDismiss(dialog: DialogInterface) {
        preferences?.let {
            val editor = preferences!!.edit()

            editor.putBoolean(OPEN_APP_PREF_KEY, isOpenAppEnabled)
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
        }
        showNotificationSwitch.setOnCheckedChangeListener { _, isChecked ->
            isNotificationEnabled = isChecked
        }

        openAppSwitch.isChecked = isOpenAppEnabled
        showNotificationSwitch.isChecked = isNotificationEnabled

        return view
    }
}