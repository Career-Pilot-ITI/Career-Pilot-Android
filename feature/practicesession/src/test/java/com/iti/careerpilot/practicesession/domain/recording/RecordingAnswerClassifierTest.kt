package com.iti.careerpilot.practicesession.domain.recording

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RecordingAnswerClassifierTest {

    @Test
    fun `very short recording is treated as unanswered`() {
        val amplitudes = List(8) { 0.4f }

        assertTrue(
            RecordingAnswerClassifier.isLikelyEmpty(
                durationMs = 700L,
                rawAmplitudes = amplitudes,
            )
        )
    }

    @Test
    fun `long silence is treated as unanswered`() {
        val amplitudes = List(200) { 0.005f }

        assertTrue(
            RecordingAnswerClassifier.isLikelyEmpty(
                durationMs = 20_000L,
                rawAmplitudes = amplitudes,
            )
        )
    }

    @Test
    fun `meaningful speech is not treated as unanswered`() {
        val amplitudes = buildList {
            repeat(80) { add(0.008f) }
            repeat(20) { add(0.2f) }
        }

        assertFalse(
            RecordingAnswerClassifier.isLikelyEmpty(
                durationMs = 10_000L,
                rawAmplitudes = amplitudes,
            )
        )
    }
}
