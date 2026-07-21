package com.iti.careerpilot.practicesession.domain.audio

import com.iti.careerpilot.practicesession.domain.audio.models.AudioTrack
import kotlinx.coroutines.flow.StateFlow

interface AudioPlayer {
    val activeTrack: StateFlow<AudioTrack>
    fun play(filePath: String, onComplete: () -> Unit)
    fun pause()
    fun resume()
    fun stop()
}