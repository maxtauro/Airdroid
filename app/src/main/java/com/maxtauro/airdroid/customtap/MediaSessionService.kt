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
import android.provider.Settings
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import com.maxtauro.airdroid.AirDroidApplication
import com.maxtauro.airdroid.orElse

@SuppressLint("LongLogTag")
class MediaSessionService : Service(), OnActiveSessionsChangedListener {

    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var mediaSessionManager: MediaSessionManager
    private lateinit var silentAudioTrack: AudioTrack

    private lateinit var mediaSessionCallback: AirdroidMediaSessionCallback

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        if (!isNotificationPermissionGranted()) {
            stopSelf()
            return super.onStartCommand(intent, flags, startId)
        }

        setupMediaSession()
        (application as AirDroidApplication).isMediaSessionServiceRunning = true
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        stopMediaSession()
        (application as AirDroidApplication).isMediaSessionServiceRunning = false
        super.onDestroy()
    }

    override fun onBind(intent: Intent?) = null

    override fun onActiveSessionsChanged(controllers: MutableList<MediaController>?) {
        Log.d(TAG, "Active Media Session Changed, ${controllers?.firstOrNull()?.packageName}")

        controllers?.let {
            updateMediaControllers(it)
            ensureAirDroidIsMainMediaController(it)
        }
    }

    private fun setupMediaSession() {
        initializeMediaSession()
        initializeMediaSessionManager()
        playSilentAudioTrack()
    }

    private fun initializeMediaSession() {
        mediaSession = MediaSessionCompat(this, "AirDroid Media Session")
        mediaSessionCallback = AirdroidMediaSessionCallback(
            context = this,
            handleEmptyMediaControllerList = ::handleNoActiveMediaSessions
        )

        val playbackState = buildPlaybackState()

        mediaSession.setPlaybackState(playbackState)
        mediaSession.setCallback(mediaSessionCallback)
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)
        mediaSession.isActive = true

        Log.d(TAG, "media session initialized")
    }

    private fun initializeMediaSessionManager() {
        mediaSessionManager =
            getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager

        mediaSessionManager.addOnActiveSessionsChangedListener(
            this, ComponentName(this, DummyNotificationListener::class.java)
        )

        updateMediaControllers()
    }

    // TODO Coroutine this
    private fun playSilentAudioTrack() {
        Thread(Runnable {
            silentAudioTrack = AudioTrack(
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

            silentAudioTrack.play()
        }).start()
    }


    // TODO, we need to be careful that this doesn't trigger an infinite loop
    //  (i.e Spotify becomes main controller, so we make AirDroid main controller, then Spotify tries again to be main controller, so we make AirDroid main controller, etc)
    private fun ensureAirDroidIsMainMediaController(controllers: MutableList<MediaController>) {
        if (controllers.isEmpty() || controllers.isAirDroidMainController()) return
        else playSilentAudioTrack()
    }

    private fun stopMediaSession() {
        if (::silentAudioTrack.isInitialized) silentAudioTrack.stop()
        if (::mediaSession.isInitialized) mediaSession.release()
    }

    private fun updateMediaControllers(mediaControllers: List<MediaController> = getActiveMediaSessions()) {
        mediaSessionCallback.updateMediaControllers(mediaControllers)
    }

    private fun getActiveMediaSessions() =
        mediaSessionManager.getActiveSessions(
            ComponentName(
                this,
                DummyNotificationListener::class.java
            )
        )

    // Revive inactive media sessions
    private fun handleNoActiveMediaSessions(mediaButtonEvent: Intent) {
        stopMediaSession()
        sendBroadcast(mediaButtonEvent)
    }

    private fun buildPlaybackState() = PlaybackStateCompat.Builder()
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
        ).build()

    private fun isNotificationPermissionGranted(): Boolean {
        applicationContext?.let {
            val cn = ComponentName(
                it, DummyNotificationListener::class.java
            )
            val secureSettings = Settings.Secure.getString(
                it.contentResolver,
                "enabled_notification_listeners"
            )
            return secureSettings != null && secureSettings.contains(cn.flattenToString())
        }.orElse {
            return false
        }
    }

    private fun MutableList<MediaController>.isAirDroidMainController() =
        !isEmpty() && first().packageName == application.packageName

    companion object {
        private const val TAG = "MediaSessionService"
    }
}
