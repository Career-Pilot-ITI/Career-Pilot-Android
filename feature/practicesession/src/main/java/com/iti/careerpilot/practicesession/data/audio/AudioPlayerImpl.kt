package com.iti.careerpilot.practicesession.data.audio

import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.iti.careerpilot.practicesession.domain.audio.AudioPlayer
import com.iti.careerpilot.practicesession.domain.audio.models.AudioPlaybackState
import com.iti.careerpilot.practicesession.domain.audio.models.AudioTrack
import com.iti.common.dispatcher.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class AudioPlayerImpl @Inject constructor(
    @param:ApplicationScope private val applicationScope: CoroutineScope,
    private val exoPlayer: ExoPlayer,
) : AudioPlayer {

    private val _activeTrack = MutableStateFlow(AudioTrack())
    override val activeTrack = _activeTrack.asStateFlow()

    private var durationJob: Job? = null
    private var onCompleteCallback: (() -> Unit)? = null

    private val playerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            super.onPlaybackStateChanged(playbackState)
            when (playbackState) {
                Player.STATE_READY -> {
                    _activeTrack.update {
                        it.copy(
                            totalDuration = exoPlayer.duration.milliseconds,
                            durationPlayed = exoPlayer.currentPosition.milliseconds,
                            isPlaying = exoPlayer.playWhenReady,
                            playbackState = if (exoPlayer.playWhenReady) AudioPlaybackState.PLAYING else AudioPlaybackState.PAUSED
                        )
                    }
                    if (exoPlayer.playWhenReady) {
                        trackDuration()
                    }
                }

                Player.STATE_ENDED -> {
                    onCompleteCallback?.invoke()
                    stop()
                }

                else -> {}
            }
        }
    }

    init {
        exoPlayer.addListener(playerListener)
    }

    override fun prepare(filePath: String) {
        if (activeTrack.value.filePath == filePath && exoPlayer.playbackState != Player.STATE_IDLE) {
            return
        }

        exoPlayer.stop()
        durationJob?.cancel()

        val mediaItem = MediaItem.fromUri(
            File(filePath).toUri()
        )

        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.playWhenReady = false

        _activeTrack.update {
            it.copy(
                isPlaying = false,
                playbackState = AudioPlaybackState.PAUSED,
                filePath = filePath
            )
        }
    }

    override fun play(filePath: String, onComplete: () -> Unit) {
        onCompleteCallback = onComplete

        if (activeTrack.value.filePath == filePath && exoPlayer.playbackState != Player.STATE_IDLE) {
            resume()
            return
        }

        exoPlayer.stop()
        durationJob?.cancel()

        val mediaItem = MediaItem.fromUri(
            File(filePath).toUri()
        )

        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.play()

        _activeTrack.update {
            it.copy(
                isPlaying = true,
                playbackState = AudioPlaybackState.PLAYING,
                filePath = filePath
            )
        }
    }

    override fun pause() {
        _activeTrack.update {
            it.copy(
                isPlaying = false,
                playbackState = if (it.playbackState == AudioPlaybackState.STOPPED) AudioPlaybackState.STOPPED else AudioPlaybackState.PAUSED
            )
        }
        durationJob?.cancel()
        exoPlayer.pause()
    }

    override fun resume() {
        if (activeTrack.value.isPlaying) {
            return
        }
        _activeTrack.update {
            it.copy(
                isPlaying = true,
                playbackState = AudioPlaybackState.PLAYING
            )
        }
        exoPlayer.play()
        trackDuration()
    }

    override fun stop() {
        _activeTrack.update {
            it.copy(
                isPlaying = false,
                durationPlayed = Duration.ZERO,
                totalDuration = Duration.ZERO,
                filePath = "",
                playbackState = AudioPlaybackState.STOPPED
            )
        }
        durationJob?.cancel()
        exoPlayer.stop()
    }

    override fun seekTo(positionMs: Long) {
        exoPlayer.seekTo(positionMs)
        _activeTrack.update {
            it.copy(
                durationPlayed = positionMs.milliseconds
            )
        }
    }

    private fun trackDuration() {
        durationJob?.cancel()
        durationJob = applicationScope.launch(Dispatchers.Main.immediate) {
            do {
                _activeTrack.update {
                    it.copy(
                        totalDuration = exoPlayer.duration.milliseconds,
                        durationPlayed = exoPlayer.currentPosition.milliseconds
                    )
                }
                delay(10L.milliseconds)
            } while (activeTrack.value.isPlaying && exoPlayer.isPlaying)
        }
    }
}
