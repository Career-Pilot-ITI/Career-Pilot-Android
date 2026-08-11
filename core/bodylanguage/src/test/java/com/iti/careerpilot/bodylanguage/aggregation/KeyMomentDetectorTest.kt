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
    fun `open look-away at session end is flushed in getKeyMoments`() {
        detector.onFaceFrame(faceSignal(1000, headYawDeg = 5f))
        detector.onFaceFrame(faceSignal(2000, headYawDeg = 25f)) // look away start
        detector.onFaceFrame(faceSignal(4000, headYawDeg = 25f)) // 2000ms duration at session end

        val moments = detector.getKeyMoments()
        assertEquals(1, moments.size)
        assertEquals(KeyMomentType.EYE_CONTACT_LOST, moments[0].type)
        assertEquals(1000L, moments[0].timestampMs) // 2000 - 1000 (session start)
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
    fun `open slouch at session end is flushed in getKeyMoments`() {
        detector.onPostureFrame(postureSignal(1000, slouchScore = 0.2f))
        detector.onPostureFrame(postureSignal(2000, slouchScore = 0.7f))
        detector.onPostureFrame(postureSignal(4000, slouchScore = 0.7f))
        detector.onPostureFrame(postureSignal(6000, slouchScore = 0.8f)) // 4000ms slouch at session end

        val moments = detector.getKeyMoments()
        assertEquals(1, moments.size)
        assertEquals(KeyMomentType.SLOUCH_START, moments[0].type)
        assertEquals(1000L, moments[0].timestampMs) // 2000 - 1000
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
        assertEquals(KeyMomentType.HAND_TO_FACE_TOUCH, moments[0].type)
    }

    @Test
    fun `open hand-to-face touch at session end is flushed in getKeyMoments`() {
        detector.onHandFrame(handSignal(1000, handToFaceTouch = false))
        detector.onHandFrame(handSignal(2000, handToFaceTouch = true))
        detector.onHandFrame(handSignal(3000, handToFaceTouch = true)) // 1000ms duration (>800ms)

        val moments = detector.getKeyMoments()
        assertEquals(1, moments.size)
        assertEquals(KeyMomentType.HAND_TO_FACE_TOUCH, moments[0].type)
        assertEquals(1000L, moments[0].timestampMs) // 2000 - 1000
        assertEquals(1000L, moments[0].durationMs)
    }

    @Test
    fun `open face lost at session end is flushed in getKeyMoments`() {
        detector.onFaceFrame(faceSignal(1000, faceDetected = true))
        detector.onFaceFrame(faceSignal(2000, faceDetected = false))
        detector.onFaceFrame(faceSignal(5000, faceDetected = false)) // 3000ms duration (>2000ms)

        val moments = detector.getKeyMoments()
        assertEquals(1, moments.size)
        assertEquals(KeyMomentType.FACE_LOST, moments[0].type)
        assertEquals(1000L, moments[0].timestampMs) // 2000 - 1000
        assertEquals(3000L, moments[0].durationMs)
    }

    @Test
    fun `session-relative timestamps normalized with non-zero start`() {
        val baseTime = 100_000L
        detector.onFaceFrame(faceSignal(baseTime + 0, headYawDeg = 0f))
        detector.onFaceFrame(faceSignal(baseTime + 2000, smileScore = 0.9f))
        detector.onHandFrame(handSignal(baseTime + 4000, handMovementScore = 0.9f))

        val moments = detector.getKeyMoments()
        assertEquals(2, moments.size)
        assertEquals(KeyMomentType.SMILE_PEAK, moments[0].type)
        assertEquals(2000L, moments[0].timestampMs)
        assertEquals(KeyMomentType.HAND_FIDGET_SPIKE, moments[1].type)
        assertEquals(4000L, moments[1].timestampMs)
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

    private fun faceSignal(
        timestampMs: Long,
        faceDetected: Boolean = true,
        headYawDeg: Float = 0f,
        smileScore: Float? = null,
    ) = FaceFrameSignal(
        timestampMs = timestampMs, faceDetected = faceDetected, smileScore = smileScore,
        headYawDeg = headYawDeg, headPitchDeg = 0f, headRollDeg = 0f,
        lookingAtCamera = null, leftEyeOpenScore = null, rightEyeOpenScore = null,
    )

    private fun postureSignal(timestampMs: Long, slouchScore: Float) = PostureFrameSignal(
        timestampMs = timestampMs, poseDetected = true, torsoLeanDeg = 0f,
        shoulderTiltDeg = 0f, slouchScore = slouchScore, movementScore = null,
    )

    private fun handSignal(
        timestampMs: Long,
        handToFaceTouch: Boolean = false,
        handMovementScore: Float? = null,
    ) = HandFrameSignal(
        timestampMs = timestampMs, handsVisible = 1,
        handToFaceTouch = handToFaceTouch, handMovementScore = handMovementScore,
    )
}
