package com.iti.careerpilot.ai.fallback

import com.iti.core.model.bodylanguage.BodyLanguageMetrics
import com.iti.core.model.bodylanguage.ConfidenceBand
import com.iti.core.model.bodylanguage.KeyMoment
import com.iti.core.model.bodylanguage.KeyMomentType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class LocalBodyLanguageFallbackEngineTest {

    private lateinit var engine: LocalBodyLanguageFallbackEngine

    @Before
    fun setup() {
        engine = LocalBodyLanguageFallbackEngine()
    }

    @Test
    fun `perfect metrics produce high score and HIGH confidence`() {
        val metrics = BodyLanguageMetrics(
            schemaVersion = 1,
            sessionDurationMs = 60_000L,
            averageSmile = 0.6f,
            maxSmile = 0.9f,
            eyeContactPercentage = 95.0f,
            timeLookingAwayMs = 1_000L,
            faceLostCount = 0,
            averageTorsoLeanDeg = 0f,
            averageShoulderTiltDeg = 0f,
            slouchPercentage = 0f,
            postureChanges = 0,
            handsVisiblePercentage = 80.0f,
            handToFaceTouchCount = 0,
            fidgetScore = 0f,
            keyMoments = emptyList(),
        )

        val eval = engine.evaluate(metrics)

        assertTrue("Overall score should be >= 80 but was ${eval.overallScore}", eval.overallScore >= 80)
        assertEquals(ConfidenceBand.HIGH, eval.confidenceBand)
        assertNotNull(eval.summary)
        assertTrue(eval.actionableTips.isNotEmpty())
    }

    @Test
    fun `low eye contact and high slouch produce LOW or MODERATE confidence`() {
        val metrics = BodyLanguageMetrics(
            schemaVersion = 1,
            sessionDurationMs = 60_000L,
            averageSmile = 0f,
            maxSmile = 0f,
            eyeContactPercentage = 20.0f,
            timeLookingAwayMs = 45_000L,
            faceLostCount = 5,
            averageTorsoLeanDeg = 15f,
            averageShoulderTiltDeg = 8f,
            slouchPercentage = 85.0f,
            postureChanges = 10,
            handsVisiblePercentage = 0f,
            handToFaceTouchCount = 6,
            fidgetScore = 0.9f,
            keyMoments = listOf(
                KeyMoment(timestampMs = 5000, type = KeyMomentType.EYE_CONTACT_LOST),
                KeyMoment(timestampMs = 10000, type = KeyMomentType.SLOUCH_START),
            ),
        )

        val eval = engine.evaluate(metrics)

        assertTrue("Overall score should be < 50 but was ${eval.overallScore}", eval.overallScore < 50)
        assertEquals(ConfidenceBand.LOW, eval.confidenceBand)
    }

    @Test
    fun `empty metrics evaluate safely without throwing`() {
        val eval = engine.evaluate(BodyLanguageMetrics.EMPTY)
        assertNotNull(eval)
        assertTrue(eval.overallScore in 0..100)
    }
}
