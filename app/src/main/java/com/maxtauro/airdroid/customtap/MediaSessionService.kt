package com.maxtauro.airdroid.customtap

import android.annotation.SuppressLint
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.MediaSessionManager.OnActiveSessionsChangedListener
import android.media.session.PlaybackState
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import com.maxtauro.airdroid.AirDroidApplication

@SuppressLint("LongLogTag")
class MediaSessionService : Service(), OnActiveSessionsChangedListener {

    private lateinit var dummyMediaSession: MediaSessionCompat
    private lateinit var dummyMediaSessionManager: MediaSessionManager
    private lateinit var dummyAudioTrack: AudioTrack

    private lateinit var mediaSessionCallback: AirdroidMediaSessionCallback

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        mediaSessionCallback = AirdroidMediaSessionCallback(context = this)

        initializeDummyMediaSession()
        initializeMediaSessionManager()
        makeMediaSessionActive()

        (application as AirDroidApplication).isMediaSessionServiceRunning = true

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onActiveSessionsChanged(controllers: MutableList<MediaController>?) {
        Log.d(TAG, "Active Media Session Changed, ${controllers?.firstOrNull()?.packageName}")

        controllers?.let {
            mediaSessionCallback.addMediaControllers(it)

            when {
                controllers.isEmpty() -> return
                controllers.first().packageName == application.packageName -> return
                else -> makeMediaSessionActive()
            }

            dummyMediaSessionManager.getActiveSessions(
                ComponentName(
                    this,
                    DummyNotificationListener::class.java
                )
            ).forEach { it1 ->
                Log.d(TAG, "Active Session: ${it1.packageName}")
            }
        }
    }

    override fun onDestroy() {
        Log.d(TAG, "service is being destroyed")

        // TODO this isn't quite right
        if (::dummyMediaSession.isInitialized) dummyMediaSession.release()
        (application as AirDroidApplication).isMediaSessionServiceRunning = false
        super.onDestroy()
    }

    override fun onBind(intent: Intent?) = null

    private fun makeMediaSessionActive() {
        startDummyMediaPlayer()
    }

    private fun initializeMediaSessionManager() {
        dummyMediaSessionManager =
            getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager

        dummyMediaSessionManager.addOnActiveSessionsChangedListener(
            this, ComponentName(this, DummyNotificationListener::class.java)
        )


        mediaSessionCallback.addMediaControllers(
            dummyMediaSessionManager.getActiveSessions(
                ComponentName(this, DummyNotificationListener::class.java)
            )
        )
    }

    // TODO Coroutine this
    private fun startDummyMediaPlayer() {
        if (::dummyMediaSession.isInitialized) {
            Thread(Runnable {
                dummyAudioTrack = AudioTrack(
                    AudioManager.STREAM_MUSIC,
                    48000,
                    AudioFormat.CHANNEL_OUT_STEREO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    AudioTrack.getMinBufferSize(
                        48000,
                        AudioFormat.CHANNEL_OUT_STEREO,
                        AudioFormat.ENCODING_PCM_16BIT
                    ),
                    AudioTrack.MODE_STREAM
                )

                dummyAudioTrack.play()
            }).start()
        }
    }

    private fun initializeDummyMediaSession() {
        dummyMediaSession = MediaSessionCompat(this, "AirDroid Media Session")

        val mStateBuilder = PlaybackStateCompat.Builder()
            .setActions(
                PlaybackStateCompat.ACTION_PLAY or
                        PlaybackStateCompat.ACTION_PAUSE or
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                        PlaybackStateCompat.ACTION_PLAY_PAUSE or
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT
            ).setState(
                PlaybackStateCompat.STATE_STOPPED,
                PlaybackState.PLAYBACK_POSITION_UNKNOWN,
                0f
            )

        dummyMediaSession.setPlaybackState(mStateBuilder.build())
        dummyMediaSession.setCallback(mediaSessionCallback)

        dummyMediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)
        dummyMediaSession.isActive = true

        Log.d(TAG, "media session initialized")
    }

    companion object {
        private const val TAG = "MediaSessionService"
    }
}