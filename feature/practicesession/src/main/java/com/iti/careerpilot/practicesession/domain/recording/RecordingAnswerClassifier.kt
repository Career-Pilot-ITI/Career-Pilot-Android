package com.iti.careerpilot.practicesession.domain.recording

import kotlin.math.ceil

/**
 * Conservative silence guard used before invoking Whisper.
 *
 * A recording is treated as unanswered only when it is extremely short or almost none of the
 * raw microphone amplitude samples rise above the noise floor. Meaningful recordings still go
 * through Whisper as before.
 */
object RecordingAnswerClassifier {

    const val NO_ANSWER_TRANSCRIPT = "User didn't answer"

    private const val MIN_RECORDING_DURATION_MS = 900L
    private const val SPEECH_AMPLITUDE_THRESHOLD = 0.025f
    private const val MIN_ACTIVE_SAMPLE_COUNT = 3
    private const val MIN_ACTIVE_SAMPLE_RATIO = 0.015f

    fun isLikelyEmpty(
        durationMs: Long,
        rawAmplitudes: List<Float>,
    ): Boolean {
        if (durationMs < MIN_RECORDING_DURATION_MS) return true
        if (rawAmplitudes.isEmpty()) return true

        val activeSamples = rawAmplitudes.count { it >= SPEECH_AMPLITUDE_THRESHOLD }
        val requiredActiveSamples = maxOf(
            MIN_ACTIVE_SAMPLE_COUNT,
            ceil(rawAmplitudes.size * MIN_ACTIVE_SAMPLE_RATIO).toInt(),
        )

        return activeSamples < requiredActiveSamples
    }
}