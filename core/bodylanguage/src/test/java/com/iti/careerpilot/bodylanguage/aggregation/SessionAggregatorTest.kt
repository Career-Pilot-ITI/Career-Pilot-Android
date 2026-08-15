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
    fun `empty session returns zero metrics and isCandidateDetected false`() {
        val metrics = aggregator.finalize()
        assertEquals(0L, metrics.sessionDurationMs)
        assertEquals(0f, metrics.averageSmile, 0.01f)
        assertEquals(0f, metrics.eyeContactPercentage, 0.01f)
        assertEquals(0f, metrics.faceDetectionPercentage, 0.01f)
        assertEquals(0f, metrics.poseDetectionPercentage, 0.01f)
        assertEquals(false, metrics.isCandidateDetected)
    }

    @Test
    fun `signals dropped when recording is not active`() {
        // isRecordingActive is false by default
        aggregator.addFace(faceSignal(timestampMs = 0, smileScore = 0.8f))
        val metrics = aggregator.finalize()
        assertEquals(0L, metrics.sessionDurationMs)
        assertEquals(0, metrics.totalFramesAnalyzed)
        assertEquals(0f, metrics.averageSmile, 0.01f)
    }

    @Test
    fun `smile average and presence computed correctly during active recording`() {
        aggregator.resumeRecording(0)
        aggregator.addFace(faceSignal(timestampMs = 0, smileScore = 0.2f))
        aggregator.addFace(faceSignal(timestampMs = 125, smileScore = 0.8f))
        aggregator.addFace(faceSignal(timestampMs = 250, smileScore = 0.6f))

        val metrics = aggregator.finalize()
        // (0.2 + 0.8 + 0.6) / 3 ≈ 0.533
        assertEquals(0.533f, metrics.averageSmile, 0.01f)
        assertEquals(0.8f, metrics.maxSmile, 0.01f)
        assertEquals(100f, metrics.faceDetectionPercentage, 0.01f)
        assertEquals(true, metrics.isCandidateDetected)
    }

    @Test
    fun `eye contact percentage tracks looking-at-camera frames`() {
        aggregator.resumeRecording(0)
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
        aggregator.resumeRecording(0)
        aggregator.addFace(faceSignal(timestampMs = 0, faceDetected = true))
        aggregator.addFace(faceSignal(timestampMs = 125, faceDetected = false))
        aggregator.addFace(faceSignal(timestampMs = 250, faceDetected = false))
        aggregator.addFace(faceSignal(timestampMs = 375, faceDetected = true))
        aggregator.addFace(faceSignal(timestampMs = 500, faceDetected = false))

        val metrics = aggregator.finalize()
        assertEquals(2, metrics.faceLostCount)
        // 2 detected out of 5 = 40%
        assertEquals(40f, metrics.faceDetectionPercentage, 0.01f)
    }

    @Test
    fun `hand-to-face counts distinct touch events`() {
        aggregator.resumeRecording(0)
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
        aggregator.resumeRecording(0)
        // 2 out of 4 frames slouching
        aggregator.addPosture(postureSignal(timestampMs = 0, slouchScore = 0.3f))
        aggregator.addPosture(postureSignal(timestampMs = 250, slouchScore = 0.6f))
        aggregator.addPosture(postureSignal(timestampMs = 500, slouchScore = 0.7f))
        aggregator.addPosture(postureSignal(timestampMs = 750, slouchScore = 0.2f))

        val metrics = aggregator.finalize()
        assertEquals(50f, metrics.slouchPercentage, 0.01f)
        assertEquals(100f, metrics.poseDetectionPercentage, 0.01f)
    }

    @Test
    fun `session duration spans first to last signal`() {
        aggregator.resumeRecording(1000)
        aggregator.addFace(faceSignal(timestampMs = 1000))
        aggregator.addPosture(postureSignal(timestampMs = 5000))
        aggregator.addHand(handSignal(timestampMs = 10000))

        val metrics = aggregator.finalize()
        assertEquals(9000L, metrics.sessionDurationMs)
    }

    @Test
    fun `pause and resume calculates cumulative active answering duration`() {
        // Window 1: 1000ms -> 5000ms (duration = 4000ms)
        aggregator.resumeRecording(1000)
        aggregator.addFace(faceSignal(timestampMs = 1000))
        aggregator.addFace(faceSignal(timestampMs = 3000))
        aggregator.pauseRecording(timestampMs = 5000)

        // Signals arriving while paused should be ignored
        aggregator.addFace(faceSignal(timestampMs = 7000, smileScore = 1.0f))

        // Window 2: 10000ms -> 16000ms (duration = 6000ms)
        aggregator.resumeRecording(timestampMs = 10000)
        aggregator.addFace(faceSignal(timestampMs = 12000))
        aggregator.addFace(faceSignal(timestampMs = 16000))

        val metrics = aggregator.finalize()
        // Total active duration = 4000ms + 6000ms = 10000ms (vs wall clock 15000ms)
        assertEquals(10000L, metrics.sessionDurationMs)
        assertEquals(0f, metrics.averageSmile, 0.01f) // Ignored smile during pause
    }

    @Test
    fun `pause closes open look-away span up to pause timestamp`() {
        aggregator.resumeRecording(1000)
        aggregator.addFace(faceSignal(timestampMs = 1000, lookingAtCamera = true))
        aggregator.addFace(faceSignal(timestampMs = 2000, lookingAtCamera = false)) // look-away start
        aggregator.pauseRecording(timestampMs = 5000) // look-away closed at 5000 (duration = 3000ms)

        aggregator.resumeRecording(timestampMs = 8000)
        aggregator.addFace(faceSignal(timestampMs = 9000, lookingAtCamera = true))

        val metrics = aggregator.finalize()
        assertEquals(3000L, metrics.timeLookingAwayMs)
    }

    @Test
    fun `multiple pause and resume cycles accumulate active duration accurately`() {
        // Window 1: 0 -> 2000 (2000ms)
        aggregator.resumeRecording(0)
        aggregator.addFace(faceSignal(timestampMs = 0))
        aggregator.pauseRecording(timestampMs = 2000)

        // Window 2: 5000 -> 8000 (3000ms)
        aggregator.resumeRecording(timestampMs = 5000)
        aggregator.addFace(faceSignal(timestampMs = 6000))
        aggregator.pauseRecording(timestampMs = 8000)

        // Window 3: 12000 -> 15000 (3000ms)
        aggregator.resumeRecording(timestampMs = 12000)
        aggregator.addFace(faceSignal(timestampMs = 15000))

        val metrics = aggregator.finalize()
        // 2000 + 3000 + 3000 = 8000ms
        assertEquals(8000L, metrics.sessionDurationMs)
    }

    @Test
    fun `reset clears all accumulators`() {
        aggregator.resumeRecording(0)
        aggregator.addFace(faceSignal(timestampMs = 0, smileScore = 0.8f))
        aggregator.reset()
        val metrics = aggregator.finalize()
        assertEquals(0f, metrics.averageSmile, 0.01f)
        assertEquals(0, metrics.faceLostCount)
        assertEquals(0L, metrics.sessionDurationMs)
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
