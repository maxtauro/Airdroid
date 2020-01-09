package com.maxtauro.airdroidcommon

enum class PreferenceKeys(val key: String) {

    NOTIFICATION_PREF_KEY("9999"),
    OPEN_APP_PREF_KEY("9998"),
    SHOULD_SHOW_SYSTEM_ALERT_PROMPT_KEY("9997"),
    DARK_MODE_BY_SYSTEM_SETTINGS_PREF_KEY("9996"),
    DARK_MODE_BY_TOGGLE_PREF_KEY("9995"),
    HAS_MIGRATED_PREF_KEY("9994"),
    ENABLE_CUSTOM_TAP_PREF_KEY("9993"),
    CUSTOM_TAP_ACTION_PREF_KEY("9992"),
    USE_AIRPOD_PRO_IMAGE_PREF_KEY("9991");


    companion object {
        private const val TAG = "PreferenceKeys"
    }
}