package com.iti.careerpilot.bodylanguage.aggregation

import com.iti.careerpilot.bodylanguage.model.FaceFrameSignal
import com.iti.careerpilot.bodylanguage.model.HandFrameSignal
import com.iti.careerpilot.bodylanguage.model.PostureFrameSignal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SessionAggregatorTest {

    private lateinit var aggregator: SessionAggregator

    @Before
    fun setup() {
        aggregator = SessionAggregator()
    }

    @Test
    fun `empty session returns zero metrics`() {
        val metrics = aggregator.finalize()
        assertEquals(0L, metrics.sessionDurationMs)
        assertEquals(0f, metrics.averageSmile, 0.01f)
        assertEquals(0f, metrics.eyeContactPercentage, 0.01f)
    }

    @Test
    fun `smile average computed correctly from face signals`() {
        aggregator.addFace(faceSignal(timestampMs = 0, smileScore = 0.2f))
        aggregator.addFace(faceSignal(timestampMs = 125, smileScore = 0.8f))
        aggregator.addFace(faceSignal(timestampMs = 250, smileScore = 0.6f))

        val metrics = aggregator.finalize()
        // (0.2 + 0.8 + 0.6) / 3 ≈ 0.533
        assertEquals(0.533f, metrics.averageSmile, 0.01f)
        assertEquals(0.8f, metrics.maxSmile, 0.01f)
    }

    @Test
    fun `eye contact percentage tracks looking-at-camera frames`() {
        aggregator.addFace(faceSignal(timestampMs = 0, lookingAtCamera = true))
        aggregator.addFace(faceSignal(timestampMs = 125, lookingAtCamera = true))
        aggregator.addFace(faceSignal(timestampMs = 250, lookingAtCamera = false))
        aggregator.addFace(faceSignal(timestampMs = 375, lookingAtCamera = true))

        val metrics = aggregator.finalize()
        // 3 out of 4 = 75%
        assertEquals(75f, metrics.eyeContactPercentage, 0.01f)
    }

    @Test
    fun `face lost count increments on transition from detected to not-detected`() {
        aggregator.addFace(faceSignal(timestampMs = 0, faceDetected = true))
        aggregator.addFace(faceSignal(timestampMs = 125, faceDetected = false))
        aggregator.addFace(faceSignal(timestampMs = 250, faceDetected = false))
        aggregator.addFace(faceSignal(timestampMs = 375, faceDetected = true))
        aggregator.addFace(faceSignal(timestampMs = 500, faceDetected = false))

        val metrics = aggregator.finalize()
        assertEquals(2, metrics.faceLostCount)
    }

    @Test
    fun `hand-to-face counts distinct touch events`() {
        aggregator.addHand(handSignal(timestampMs = 0, handToFaceTouch = false))
        aggregator.addHand(handSignal(timestampMs = 250, handToFaceTouch = true))
        aggregator.addHand(handSignal(timestampMs = 500, handToFaceTouch = true))
        aggregator.addHand(handSignal(timestampMs = 750, handToFaceTouch = false))
        aggregator.addHand(handSignal(timestampMs = 1000, handToFaceTouch = true))

        val metrics = aggregator.finalize()
        assertEquals(2, metrics.handToFaceTouchCount)
    }

    @Test
    fun `slouch percentage computed correctly`() {
        // 2 out of 4 frames slouching
        aggregator.addPosture(postureSignal(timestampMs = 0, slouchScore = 0.3f))
        aggregator.addPosture(postureSignal(timestampMs = 250, slouchScore = 0.6f))
        aggregator.addPosture(postureSignal(timestampMs = 500, slouchScore = 0.7f))
        aggregator.addPosture(postureSignal(timestampMs = 750, slouchScore = 0.2f))

        val metrics = aggregator.finalize()
        assertEquals(50f, metrics.slouchPercentage, 0.01f)
    }

    @Test
    fun `session duration spans first to last signal`() {
        aggregator.addFace(faceSignal(timestampMs = 1000))
        aggregator.addPosture(postureSignal(timestampMs = 5000))
        aggregator.addHand(handSignal(timestampMs = 10000))

        val metrics = aggregator.finalize()
        assertEquals(9000L, metrics.sessionDurationMs)
    }

    @Test
    fun `reset clears all accumulators`() {
        aggregator.addFace(faceSignal(timestampMs = 0, smileScore = 0.8f))
        aggregator.reset()
        val metrics = aggregator.finalize()
        assertEquals(0f, metrics.averageSmile, 0.01f)
        assertEquals(0, metrics.faceLostCount)
    }

    // --- Helpers ---

    private fun faceSignal(
        timestampMs: Long = 0,
        faceDetected: Boolean = true,
        smileScore: Float? = null,
        lookingAtCamera: Boolean? = null,
    ) = FaceFrameSignal(
        timestampMs = timestampMs,
        faceDetected = faceDetected,
        smileScore = smileScore,
        headYawDeg = null,
        headPitchDeg = null,
        headRollDeg = null,
        lookingAtCamera = lookingAtCamera,
        leftEyeOpenScore = null,
        rightEyeOpenScore = null,
    )

    private fun postureSignal(
        timestampMs: Long = 0,
        slouchScore: Float? = null,
    ) = PostureFrameSignal(
        timestampMs = timestampMs,
        poseDetected = true,
        torsoLeanDeg = 0f,
        shoulderTiltDeg = 0f,
        slouchScore = slouchScore,
        movementScore = null,
    )

    private fun handSignal(
        timestampMs: Long = 0,
        handToFaceTouch: Boolean = false,
    ) = HandFrameSignal(
        timestampMs = timestampMs,
        handsVisible = 1,
        handToFaceTouch = handToFaceTouch,
        handMovementScore = null,
    )
}
