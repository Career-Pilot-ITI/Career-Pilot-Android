package com.iti.careerpilot.practicesession.domain.recording.models

import kotlin.time.Duration

data class RecordingDetails(
    val duration: Duration = Duration.ZERO,
    val amplitudes: List<Float> = emptyList(),
    val filePath: String? = null,
    val isRecording: Boolean = false
)