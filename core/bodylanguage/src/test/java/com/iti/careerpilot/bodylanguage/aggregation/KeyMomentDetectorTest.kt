package com.iti.careerpilot.bodylanguage.aggregation

import com.iti.careerpilot.bodylanguage.model.FaceFrameSignal
import com.iti.careerpilot.bodylanguage.model.HandFrameSignal
import com.iti.careerpilot.bodylanguage.model.PostureFrameSignal
import com.iti.core.model.bodylanguage.KeyMomentType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class KeyMomentDetectorTest {

    private lateinit var detector: KeyMomentDetector

    @Before
    fun setup() {
        detector = KeyMomentDetector()
    }

    @Test
    fun `look-away shorter than threshold produces no moment`() {
        detector.onFaceFrame(faceSignal(0, headYawDeg = 25f))
        detector.onFaceFrame(faceSignal(500, headYawDeg = 25f))
        detector.onFaceFrame(faceSignal(1000, headYawDeg = 5f)) // back

        assertTrue(detector.getKeyMoments().isEmpty())
    }

    @Test
    fun `look-away exceeding threshold produces moment with correct duration`() {
        detector.onFaceFrame(faceSignal(0, headYawDeg = 25f))
        detector.onFaceFrame(faceSignal(500, headYawDeg = 25f))
        detector.onFaceFrame(faceSignal(1000, headYawDeg = 30f))
        detector.onFaceFrame(faceSignal(1500, headYawDeg = 28f))
        detector.onFaceFrame(faceSignal(2000, headYawDeg = 5f)) // back

        val moments = detector.getKeyMoments()
        assertEquals(1, moments.size)
        assertEquals(KeyMomentType.EYE_CONTACT_LOST, moments[0].type)
        assertEquals(0L, moments[0].timestampMs)
        assertEquals(2000L, moments[0].durationMs)
    }

    @Test
    fun `slouch below threshold produces no moment`() {
        detector.onPostureFrame(postureSignal(0, slouchScore = 0.3f))
        detector.onPostureFrame(postureSignal(1000, slouchScore = 0.3f))
        detector.onPostureFrame(postureSignal(2000, slouchScore = 0.2f))

        assertTrue(detector.getKeyMoments().isEmpty())
    }

    @Test
    fun `sustained slouch produces moment`() {
        detector.onPostureFrame(postureSignal(0, slouchScore = 0.6f))
        detector.onPostureFrame(postureSignal(1000, slouchScore = 0.7f))
        detector.onPostureFrame(postureSignal(2000, slouchScore = 0.6f))
        detector.onPostureFrame(postureSignal(3000, slouchScore = 0.8f))
        detector.onPostureFrame(postureSignal(4000, slouchScore = 0.2f)) // ended

        val moments = detector.getKeyMoments()
        assertEquals(1, moments.size)
        assertEquals(KeyMomentType.SLOUCH_START, moments[0].type)
        assertEquals(4000L, moments[0].durationMs)
    }

    @Test
    fun `brief hand-to-face touch below threshold produces no moment`() {
        detector.onHandFrame(handSignal(0, handToFaceTouch = true))
        detector.onHandFrame(handSignal(500, handToFaceTouch = false))

        assertTrue(detector.getKeyMoments().isEmpty())
    }

    @Test
    fun `sustained hand-to-face touch produces moment`() {
        detector.onHandFrame(handSignal(0, handToFaceTouch = true))
        detector.onHandFrame(handSignal(500, handToFaceTouch = true))
        detector.onHandFrame(handSignal(1000, handToFaceTouch = false))

        val moments = detector.getKeyMoments()
        assertEquals(1, moments.size)
        assertEquals(KeyMomentType.HAND_FIDGET_SPIKE, moments[0].type)
    }

    @Test
    fun `reset clears all state and moments`() {
        detector.onFaceFrame(faceSignal(0, headYawDeg = 30f))
        detector.onFaceFrame(faceSignal(2000, headYawDeg = 5f))
        assertEquals(1, detector.getKeyMoments().size)

        detector.reset()
        assertTrue(detector.getKeyMoments().isEmpty())
    }

    // --- Helpers ---

    private fun faceSignal(timestampMs: Long, headYawDeg: Float = 0f) = FaceFrameSignal(
        timestampMs = timestampMs, faceDetected = true, smileScore = null,
        headYawDeg = headYawDeg, headPitchDeg = 0f, headRollDeg = 0f,
        lookingAtCamera = null, leftEyeOpenScore = null, rightEyeOpenScore = null,
    )

    private fun postureSignal(timestampMs: Long, slouchScore: Float) = PostureFrameSignal(
        timestampMs = timestampMs, poseDetected = true, torsoLeanDeg = 0f,
        shoulderTiltDeg = 0f, slouchScore = slouchScore, movementScore = null,
    )

    private fun handSignal(timestampMs: Long, handToFaceTouch: Boolean) = HandFrameSignal(
        timestampMs = timestampMs, handsVisible = 1,
        handToFaceTouch = handToFaceTouch, handMovementScore = null,
    )
}
