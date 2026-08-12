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

        assertEquals(74, eval.eyeContact.score)
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
            eyeContactPercentage = 15.0f,
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

    @Test
    fun `eye contact continuous piecewise scoring maintains continuity at boundaries`() {
        val baseMetrics = BodyLanguageMetrics.EMPTY.copy(
            faceDetectionPercentage = 100f,
            poseDetectionPercentage = 100f,
        )

        // E = 0% -> 30
        assertEquals(30, engine.evaluate(baseMetrics.copy(eyeContactPercentage = 0f)).eyeContact.score)

        // Boundary around 40%: E = 39.99% -> 70, E = 40.0% -> 70, E = 40.01% -> 70
        assertEquals(70, engine.evaluate(baseMetrics.copy(eyeContactPercentage = 39.99f)).eyeContact.score)
        assertEquals(70, engine.evaluate(baseMetrics.copy(eyeContactPercentage = 40.0f)).eyeContact.score)
        assertEquals(70, engine.evaluate(baseMetrics.copy(eyeContactPercentage = 40.01f)).eyeContact.score)

        // Boundary around 50%: E = 49.99% -> 88, E = 50.0% -> 88, E = 50.01% -> 88
        assertEquals(88, engine.evaluate(baseMetrics.copy(eyeContactPercentage = 49.99f)).eyeContact.score)
        assertEquals(88, engine.evaluate(baseMetrics.copy(eyeContactPercentage = 50.0f)).eyeContact.score)
        assertEquals(88, engine.evaluate(baseMetrics.copy(eyeContactPercentage = 50.01f)).eyeContact.score)

        // Boundary around 75%: E = 74.99% -> 98, E = 75.0% -> 98, E = 75.01% -> 98
        assertEquals(98, engine.evaluate(baseMetrics.copy(eyeContactPercentage = 74.99f)).eyeContact.score)
        assertEquals(98, engine.evaluate(baseMetrics.copy(eyeContactPercentage = 75.0f)).eyeContact.score)
        assertEquals(98, engine.evaluate(baseMetrics.copy(eyeContactPercentage = 75.01f)).eyeContact.score)

        // E = 100% -> 70
        assertEquals(70, engine.evaluate(baseMetrics.copy(eyeContactPercentage = 100.0f)).eyeContact.score)
    }

    @Test
    fun `hand gesture scoring follows continuous visibility formula and penalties`() {
        val baseMetrics = BodyLanguageMetrics.EMPTY.copy(
            faceDetectionPercentage = 100f,
            poseDetectionPercentage = 100f,
        )

        // V = 0% -> 65
        assertEquals(65, engine.evaluate(baseMetrics.copy(handsVisiblePercentage = 0f)).handGestures.score)
        // V = 25% -> 92
        assertEquals(92, engine.evaluate(baseMetrics.copy(handsVisiblePercentage = 25f)).handGestures.score)
        // V = 40% -> 92
        assertEquals(92, engine.evaluate(baseMetrics.copy(handsVisiblePercentage = 40f)).handGestures.score)
        // V = 50% -> 92
        assertEquals(92, engine.evaluate(baseMetrics.copy(handsVisiblePercentage = 50f)).handGestures.score)
        // V = 100% -> 75
        assertEquals(75, engine.evaluate(baseMetrics.copy(handsVisiblePercentage = 100f)).handGestures.score)

        // Fidget penalty > 0.30f: deduct (fidgetScore * 35).toInt()
        // V = 50f (base 92), fidget = 0.40f -> deduction = (0.40 * 35).toInt() = 14 -> score = 78
        assertEquals(78, engine.evaluate(baseMetrics.copy(handsVisiblePercentage = 50f, fidgetScore = 0.40f)).handGestures.score)

        // Clamping to min 15: V = 0f (base 65), touches = 10 (penalty 120) -> 65 - 120 = -55 -> clamped to 15
        assertEquals(15, engine.evaluate(baseMetrics.copy(handsVisiblePercentage = 0f, handToFaceTouchCount = 10)).handGestures.score)
    }

    @Test
    fun `confidence band coverage maps HIGH, MODERATE, and LOW deterministically`() {
        val baseMetrics = BodyLanguageMetrics.EMPTY.copy(
            faceDetectionPercentage = 100f,
            poseDetectionPercentage = 100f,
        )

        // HIGH: overallScore >= 70 && minDimensionScore >= 45
        val highEval = engine.evaluate(baseMetrics.copy(
            eyeContactPercentage = 65f, // eye contact = 94
            handsVisiblePercentage = 35f, // hands = 92
            averageTorsoLeanDeg = 8f, // posture = 95
            averageSmile = 0.35f, // facial = 82
        ))
        assertTrue(highEval.overallScore >= 70)
        assertEquals(ConfidenceBand.HIGH, highEval.confidenceBand)

        // MODERATE: overallScore >= 70 but one dimension < 45
        val modWithLowDim = engine.evaluate(baseMetrics.copy(
            eyeContactPercentage = 75f, // eye contact = 98
            handsVisiblePercentage = 0f,
            handToFaceTouchCount = 5, // hands = 15
            averageTorsoLeanDeg = 8f, // posture = 95
            averageSmile = 0.5f, // facial = 82
        ))
        assertTrue("overallScore should be >= 70 but was ${modWithLowDim.overallScore}", modWithLowDim.overallScore >= 70)
        assertTrue("hands score should be < 45 but was ${modWithLowDim.handGestures.score}", modWithLowDim.handGestures.score < 45)
        assertEquals(ConfidenceBand.MODERATE, modWithLowDim.confidenceBand)
    }

    @Test
    fun `facial expression evaluation uses delivery dynamics formula and objective phrasing`() {
        val baseMetrics = BodyLanguageMetrics.EMPTY.copy(
            faceDetectionPercentage = 100f,
            poseDetectionPercentage = 100f,
        )

        // Base 78 with low animation (0.0f expressiveness, 0 peaks)
        val lowAnim = engine.evaluate(baseMetrics.copy(averageSmile = 0f))
        assertEquals(78, lowAnim.facialExpression.score)
        assertTrue(lowAnim.facialExpression.observation.contains("Subdued facial expressiveness"))

        // Base 78 + animation bonus 10 (0.25f expressiveness in 0.08..0.40, 0 peaks) -> 88
        val engAnim = engine.evaluate(baseMetrics.copy(averageSmile = 0.25f))
        assertEquals(88, engAnim.facialExpression.score)
        assertTrue(engAnim.facialExpression.observation.contains("Engaged, natural delivery animation"))

        // Base 78 + peak bonus 6 (1 peak) + animation bonus 10 (0.35f in 0.08..0.40) -> 94
        val peakAnim = engine.evaluate(baseMetrics.copy(
            averageSmile = 0.35f,
            keyMoments = listOf(KeyMoment(timestampMs = 5000, type = KeyMomentType.SMILE_PEAK)),
        ))
        assertEquals(94, peakAnim.facialExpression.score)
        assertTrue(peakAnim.facialExpression.observation.contains("Dynamic, animated delivery"))

        // Ensure no subjective emotion inference terms remain in evaluation observations
        val allEvaluations = listOf(lowAnim, engAnim, peakAnim)
        allEvaluations.forEach { eval ->
            val obs = eval.facialExpression.observation.lowercase()
            assertTrue(!obs.contains("warmth"))
            assertTrue(!obs.contains("anxiety"))
            assertTrue(!obs.contains("nervous"))
        }
    }
}

