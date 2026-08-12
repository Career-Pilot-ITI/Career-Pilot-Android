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
    fun `optimal metrics with natural gaze and forward lean produce high score and HIGH confidence`() {
        val metrics = BodyLanguageMetrics(
            schemaVersion = 1,
            sessionDurationMs = 60_000L,
            faceDetectionPercentage = 95.0f,
            poseDetectionPercentage = 90.0f,
            handsDetectionPercentage = 80.0f,
            totalFramesAnalyzed = 400,
            averageSmile = 0.35f,
            maxSmile = 0.8f,
            eyeContactPercentage = 65.0f, // Optimal range 50-75%
            timeLookingAwayMs = 21_000L,
            faceLostCount = 0,
            averageTorsoLeanDeg = 8.0f, // Optimal engagement lean +5 to +15 deg
            averageShoulderTiltDeg = 1.5f,
            slouchPercentage = 0f,
            postureChanges = 1,
            handsVisiblePercentage = 35.0f, // Optimal gesture range 25-50%
            handToFaceTouchCount = 0,
            fidgetScore = 0.05f,
            keyMoments = listOf(
                KeyMoment(timestampMs = 10_000L, type = KeyMomentType.SMILE_PEAK, intensity = 0.8f),
            ),
        )

        val eval = engine.evaluate(metrics)

        assertTrue("Overall score should be >= 85 but was ${eval.overallScore}", eval.overallScore >= 85)
        assertEquals(ConfidenceBand.HIGH, eval.confidenceBand)
        assertTrue(eval.eyeContact.score >= 90)
        assertTrue(eval.posture.score >= 90)
        assertTrue(eval.handGestures.score >= 90)
        assertNotNull(eval.summary)
        assertTrue(eval.actionableTips.isNotEmpty())
    }

    @Test
    fun `continuous stare greater than 85 percent triggers stare observation and reduced eye score`() {
        val metrics = BodyLanguageMetrics(
            schemaVersion = 1,
            sessionDurationMs = 60_000L,
            faceDetectionPercentage = 95.0f,
            poseDetectionPercentage = 90.0f,
            handsDetectionPercentage = 80.0f,
            totalFramesAnalyzed = 400,
            averageSmile = 0.3f,
            maxSmile = 0.5f,
            eyeContactPercentage = 96.0f, // Rigid continuous staring
            timeLookingAwayMs = 2_000L,
            faceLostCount = 0,
            averageTorsoLeanDeg = 5.0f,
            averageShoulderTiltDeg = 0f,
            slouchPercentage = 0f,
            postureChanges = 0,
            handsVisiblePercentage = 30.0f,
            handToFaceTouchCount = 0,
            fidgetScore = 0f,
            keyMoments = emptyList(),
        )

        val eval = engine.evaluate(metrics)

        assertEquals(68, eval.eyeContact.score)
        assertTrue(eval.eyeContact.observation.contains("focus", ignoreCase = true) || eval.eyeContact.observation.contains("staring", ignoreCase = true))
        assertTrue(eval.eyeContact.tip.contains("breaks", ignoreCase = true) || eval.eyeContact.tip.contains("Blink", ignoreCase = true))
    }

    @Test
    fun `low eye contact less than 40 percent triggers lookaway observation and low score`() {
        val metrics = BodyLanguageMetrics(
            schemaVersion = 1,
            sessionDurationMs = 60_000L,
            faceDetectionPercentage = 80.0f,
            poseDetectionPercentage = 80.0f,
            handsDetectionPercentage = 0.0f,
            totalFramesAnalyzed = 400,
            averageSmile = 0f,
            maxSmile = 0f,
            eyeContactPercentage = 25.0f,
            timeLookingAwayMs = 45_000L,
            faceLostCount = 3,
            averageTorsoLeanDeg = 0f,
            averageShoulderTiltDeg = 0f,
            slouchPercentage = 0f,
            postureChanges = 0,
            handsVisiblePercentage = 0f,
            handToFaceTouchCount = 0,
            fidgetScore = 0f,
            keyMoments = emptyList(),
        )

        val eval = engine.evaluate(metrics)

        assertTrue("Eye contact score should be < 50 but was ${eval.eyeContact.score}", eval.eyeContact.score < 50)
        assertTrue(eval.eyeContact.observation.contains("Low eye contact", ignoreCase = true))
    }

    @Test
    fun `hand to face touches penalize hand gestures score`() {
        val metricsNoTouches = BodyLanguageMetrics.EMPTY.copy(
            sessionDurationMs = 60_000L,
            faceDetectionPercentage = 90f,
            poseDetectionPercentage = 90f,
            handsDetectionPercentage = 80f,
            handsVisiblePercentage = 35f,
            handToFaceTouchCount = 0,
            fidgetScore = 0f,
        )
        val metricsWithTouches = metricsNoTouches.copy(
            handToFaceTouchCount = 3,
        )

        val evalClean = engine.evaluate(metricsNoTouches)
        val evalTouched = engine.evaluate(metricsWithTouches)

        assertTrue(evalClean.handGestures.score > evalTouched.handGestures.score)
        assertEquals(36, evalClean.handGestures.score - evalTouched.handGestures.score)
        assertTrue(evalTouched.handGestures.observation.contains("hand-to-face", ignoreCase = true))
    }

    @Test
    fun `low eye contact and high slouch produce LOW confidence`() {
        val metrics = BodyLanguageMetrics(
            schemaVersion = 1,
            sessionDurationMs = 60_000L,
            faceDetectionPercentage = 80.0f,
            poseDetectionPercentage = 80.0f,
            handsDetectionPercentage = 0.0f,
            totalFramesAnalyzed = 400,
            averageSmile = 0f,
            maxSmile = 0f,
            eyeContactPercentage = 20.0f,
            timeLookingAwayMs = 45_000L,
            faceLostCount = 5,
            averageTorsoLeanDeg = 25f,
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

    @Test
    fun `session with zero detected landmarks evaluates as candidate out of camera view`() {
        val metrics = BodyLanguageMetrics(
            schemaVersion = 1,
            sessionDurationMs = 30_000L,
            averageSmile = 0f,
            maxSmile = 0f,
            eyeContactPercentage = 0f,
            timeLookingAwayMs = 0L,
            faceLostCount = 1,
            averageTorsoLeanDeg = 0f,
            averageShoulderTiltDeg = 0f,
            slouchPercentage = 0f,
            postureChanges = 0,
            handsVisiblePercentage = 0f,
            handToFaceTouchCount = 0,
            fidgetScore = 0f,
            keyMoments = emptyList(),
        )

        val eval = engine.evaluate(metrics)

        assertEquals(0, eval.overallScore)
        assertEquals(0, eval.eyeContact.score)
        assertEquals(0, eval.posture.score)
        assertEquals(0, eval.facialExpression.score)
        assertEquals(0, eval.handGestures.score)
        assertEquals(ConfidenceBand.LOW, eval.confidenceBand)
        assertTrue(eval.summary.contains("out of camera view", ignoreCase = true))
    }
}
