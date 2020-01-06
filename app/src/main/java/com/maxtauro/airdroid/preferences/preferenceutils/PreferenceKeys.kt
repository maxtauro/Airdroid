package com.maxtauro.airdroid.preferences.preferenceutils

enum class PreferenceKeys(val key: String) {

    NOTIFICATION_PREF_KEY("9999"),
    OPEN_APP_PREF_KEY("9998"),
    SHOULD_SHOW_SYSTEM_ALERT_PROMPT_KEY("9997"),
    DARK_MODE_BY_SYSTEM_SETTINGS_PREF_KEY("9996"),
    DARK_MODE_BY_TOGGLE_PREF_KEY("9995"),
    HAS_MIGRATED_PREF_KEY("9994"),
    NOTIFICATION_ACCESS_PREF_KEY("9993");

    companion object {
        private const val TAG = "PreferenceKeys"
    }
}