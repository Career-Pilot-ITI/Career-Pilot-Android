package com.iti.careerpilot.practicesession.domain.audio.models

import kotlin.time.Duration

data class AudioTrack(
    val totalDuration: Duration = Duration.ZERO,
    val durationPlayed: Duration = Duration.ZERO,
    val isPlaying: Boolean = false,
    val filePath: String = "",
)