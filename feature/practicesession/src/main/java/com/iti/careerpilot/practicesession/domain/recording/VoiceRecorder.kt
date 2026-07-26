package com.iti.careerpilot.practicesession.domain.recording

import com.iti.careerpilot.practicesession.domain.recording.models.RecordingDetails
import kotlinx.coroutines.flow.StateFlow

interface VoiceRecorder {
    val recordingDetails: StateFlow<RecordingDetails>
    fun start()
    fun pause()
    fun stop()
    fun resume()
    fun cancel()
}