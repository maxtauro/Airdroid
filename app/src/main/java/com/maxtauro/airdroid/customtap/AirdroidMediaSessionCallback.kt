package com.maxtauro.airdroid.customtap

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.session.MediaController
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import android.view.KeyEvent
import android.view.KeyEvent.*
import androidx.preference.PreferenceManager
import com.maxtauro.airdroid.preferences.preferenceutils.PreferenceKeys

@SuppressLint("LongLogTag")
class AirdroidMediaSessionCallback(
    private val context: Context,
    private val mediaControllers: MutableList<MediaController> = mutableListOf()
) : MediaSessionCompat.Callback() {

    private val preferences: SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(context)

    private val doubleTapPref: CustomTapOptions
        get() = CustomTapOptions.valueOf(
            preferences.getString(
                PreferenceKeys.CUSTOM_TAP_ACTION_PREF_KEY.key,
                CustomTapOptions.OFF.toString()
            )!!
        )

    override fun onMediaButtonEvent(mediaButtonEvent: Intent?): Boolean {
        Log.d(TAG, "$mediaButtonEvent")

        val keyEvent =
            mediaButtonEvent?.getParcelableExtra<KeyEvent>(Intent.EXTRA_KEY_EVENT)
                ?: return false

        // TODO if there is no active media session, we need to start the most recent one

        keyEvent.let {
            return when (doubleTapPref) {
                CustomTapOptions.OFF -> onDefaultMediaButtonEvent(it)
                CustomTapOptions.PLAY_PAUSE -> onPlayPauseMediaButtonEvent(it.action)
                CustomTapOptions.NEXT -> onSkipToNextMediaButtonEvent(it.action)
                CustomTapOptions.PREV -> onPrevMediaButtonEvent(it.action)
                CustomTapOptions.GOOGLE_ASSISTANT -> onGoogleAssistantMediaButtonEvent()
            }
        }
    }

    fun addMediaControllers(newMediaControllers: List<MediaController>) {
        newMediaControllers.forEach { mediaController ->
            if (mediaController.packageName != context.applicationContext.packageName && mediaControllers.none {
                    it.packageName.contains(
                        mediaController.packageName
                    )
                }) {
                mediaControllers.add(mediaController)
            }
        }
    }

    private fun onDefaultMediaButtonEvent(keyEvent: KeyEvent): Boolean {
        Log.d(TAG, "onDefaultMediaButtonEvent")
        dispatchKeyEvent(keyEvent)
        return true
    }

    private fun onPlayPauseMediaButtonEvent(keyAction: Int): Boolean {
        Log.d(TAG, "onPlayPauseMediaButtonEvent")
        dispatchKeyEvent(KeyEvent(keyAction, KEYCODE_MEDIA_PLAY_PAUSE))
        return true
    }

    private fun onSkipToNextMediaButtonEvent(keyAction: Int): Boolean {
        Log.d(TAG, "onSkipToNextMediaButtonEvent")
        dispatchKeyEvent(KeyEvent(keyAction, KEYCODE_MEDIA_NEXT))
        return true
    }

    private fun onPrevMediaButtonEvent(keyAction: Int): Boolean {
        Log.d(TAG, "onPrevMediaButtonEvent")
        dispatchKeyEvent(KeyEvent(keyAction, KEYCODE_MEDIA_PREVIOUS))
        return true
    }

    private fun onGoogleAssistantMediaButtonEvent(): Boolean {
        Log.d(TAG, "onGoogleAssistantMediaButtonEvent")
        Intent(Intent.ACTION_VOICE_COMMAND).also {
            it.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(it)
            return true
        }
    }

    private fun dispatchKeyEvent(keyEvent: KeyEvent) {
        mediaControllers.forEach {
            Log.d(TAG, "Dispatching key event: $keyEvent to controller: ${it.packageName}")
            it.dispatchMediaButtonEvent(keyEvent)
        }
    }

    companion object {
        private const val TAG = "AirdroidMediaSessionCallback"
    }
}