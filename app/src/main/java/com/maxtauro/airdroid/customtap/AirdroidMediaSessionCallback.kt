package com.maxtauro.airdroid.customtap

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.session.MediaController
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import android.view.KeyEvent
import android.view.KeyEvent.*
import android.widget.Toast
import androidx.preference.PreferenceManager
import com.maxtauro.airdroid.BuildConfig
import com.maxtauro.airdroidcommon.PreferenceKeys
import java.util.prefs.InvalidPreferencesFormatException


@SuppressLint("LongLogTag")
class AirdroidMediaSessionCallback(
    private val context: Context,
    private val mediaControllers: MutableList<MediaController> = mutableListOf(),
    private val handleEmptyMediaControllerList: (Intent) -> Unit
) : MediaSessionCompat.Callback() {

    private val preferences: SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(context)

    // TODO tidy this up
    private val doubleTapPref: CustomTapOptions
        get() = CustomTapOptions.valueOf(
            preferences.getString(
                PreferenceKeys.CUSTOM_TAP_ACTION_PREF_KEY.key,
                CustomTapOptions.OFF.toString()
            ) ?: throw InvalidPreferencesFormatException(
                "Cannot parse custom tap preference: ${preferences.getString(
                    PreferenceKeys.CUSTOM_TAP_ACTION_PREF_KEY.key,
                    CustomTapOptions.OFF.toString()
                )}"
            )
        )

    override fun onMediaButtonEvent(mediaButtonEvent: Intent?): Boolean {
        triggerDebugEventsForButtonEvent(mediaButtonEvent)

        return if (mediaControllers.isEmpty()) handleMediaButtonEventEmptyControllerList(
            mediaButtonEvent
        )
        else handleMediaButtonEvent(mediaButtonEvent)
    }

    fun updateMediaControllers(newMediaControllers: List<MediaController>) {
        // TODO cleanup
        newMediaControllers.forEach { mediaController ->
            if (mediaController.packageName != context.applicationContext.packageName && mediaControllers.none {
                    it.packageName.contains(
                        mediaController.packageName
                    )
                }) {
                mediaControllers.add(mediaController)
            }
            // Replace old media controllers with more upto date controllers
            else if (!mediaControllers.none {
                    it.packageName.contains(
                        mediaController.packageName
                    )
                }
            ) {
                mediaControllers.removeAll {
                    it.packageName.contains(
                        mediaController.packageName
                    )
                }
                mediaControllers.add(mediaController)
            }
        }

        val msg = "Has the following Media Controllers: \n"
        val sb = StringBuilder()
        sb.append(msg)
        mediaControllers.forEach {
            sb.append("${it.packageName} \n")
        }

        Log.d(TAG, sb.toString())
    }

    private fun handleMediaButtonEvent(mediaButtonEvent: Intent?): Boolean {
        val keyEvent =
            mediaButtonEvent?.getParcelableExtra<KeyEvent>(Intent.EXTRA_KEY_EVENT)
                ?: return false

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

    private fun handleMediaButtonEventEmptyControllerList(mediaButtonEvent: Intent?): Boolean {
        /* If there is no media session active we will:
             - kill the current AirDroid one
             - broadcast the media button event
             - restart the AirDroid one
          */
        Log.d(TAG, "Media Controller List is empty")
        mediaButtonEvent?.let { handleEmptyMediaControllerList(it) }
        return false
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

    private fun triggerDebugEventsForButtonEvent(mediaButtonEvent: Intent?) {
        Log.d(TAG, "$mediaButtonEvent")

        // TODO remove, temp for debug
        val msg = "Has the following Media Controllers: \n"
        val sb = StringBuilder()
        sb.append(msg)
        mediaControllers.forEach {
            sb.append("${it.packageName} \n")
        }

        Log.d(TAG, sb.toString())

        if (BuildConfig.BUILD_TYPE == "debug") {
            playShortVibration()
            showMediaButtonToast(mediaButtonEvent)
        }
    }

    private fun showMediaButtonToast(mediaButtonEvent: Intent?) {
        Toast.makeText(context, "$mediaButtonEvent", Toast.LENGTH_SHORT).show()
    }

    private fun playShortVibration() {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }

    companion object {
        private const val TAG = "AirdroidMediaSessionCallback"
    }
}