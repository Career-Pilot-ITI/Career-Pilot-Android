package com.iti.careerpilot.bodylanguage.signal

import com.google.mediapipe.framework.image.MPImage
import com.google.mediapipe.tasks.components.containers.Landmark
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Optional
import kotlin.math.atan2

class PostureSignalExtractorTest {

    private lateinit var extractor: PostureSignalExtractor

    @Before
    fun setup() {
        extractor = PostureSignalExtractor()
    }

    private fun mockPoseResult(
        worldLandmarks: List<List<Landmark>> = emptyList(),
        landmarks: List<List<NormalizedLandmark>> = emptyList(),
        timestampMs: Long = 0L,
    ): PoseLandmarkerResult {
        return object : PoseLandmarkerResult() {
            override fun landmarks(): List<List<NormalizedLandmark>> = landmarks
            override fun worldLandmarks(): List<List<Landmark>> = worldLandmarks
            override fun segmentationMasks(): Optional<List<MPImage>> = Optional.empty()
            override fun timestampMs(): Long = timestampMs
        }
    }

    @Test
    fun `empty world landmarks returns poseDetected false`() {
        val result = mockPoseResult()
        val signal = extractor.extract(result, 1000L)
        assertFalse(signal.poseDetected)
        assertNull(signal.torsoLeanDeg)
        assertNull(signal.shoulderTiltDeg)
        assertNull(signal.slouchScore)
    }

    @Test
    fun `low shoulder visibility returns poseDetected false`() {
        val worldLandmarks = MutableList(33) {
            Landmark.create(0f, 0f, 0f, Optional.of(0.9f), Optional.of(0.9f))
        }
        // Left shoulder (11) low visibility
        worldLandmarks[11] = Landmark.create(0f, 0f, 0f, Optional.of(0.3f), Optional.of(0.9f))

        val result = mockPoseResult(worldLandmarks = listOf(worldLandmarks))
        val signal = extractor.extract(result, 1000L)
        assertFalse(signal.poseDetected)
    }

    @Test
    fun `sagittal lean computes positive angle when leaning forward`() {
        val worldLandmarks = MutableList(33) {
            Landmark.create(0f, 0f, 0f, Optional.of(0.9f), Optional.of(0.9f))
        }
        // Hips at y = 0.5m, z = 0m
        worldLandmarks[23] = Landmark.create(-0.15f, 0.5f, 0f, Optional.of(0.9f), Optional.of(0.9f)) // Left hip
        worldLandmarks[24] = Landmark.create(0.15f, 0.5f, 0f, Optional.of(0.9f), Optional.of(0.9f))  // Right hip

        // Shoulders leaning forward (dz > 0): y = 0.0m (higher), z = 0.15m (closer/forward)
        worldLandmarks[11] = Landmark.create(-0.2f, 0.0f, 0.15f, Optional.of(0.9f), Optional.of(0.9f)) // Left shoulder
        worldLandmarks[12] = Landmark.create(0.2f, 0.0f, 0.15f, Optional.of(0.9f), Optional.of(0.9f))  // Right shoulder

        val result = mockPoseResult(worldLandmarks = listOf(worldLandmarks))
        val signal = extractor.extract(result, 1000L)
        assertTrue(signal.poseDetected)
        val expectedLean = Math.toDegrees(atan2(0.15, 0.5)).toFloat()
        val torsoLean = requireNotNull(signal.torsoLeanDeg)
        assertEquals(expectedLean, torsoLean, 0.5f)
        assertTrue("Lean angle should be positive for forward lean", torsoLean > 0f)
    }

    @Test
    fun `sagittal lean computes negative angle when leaning backward`() {
        val worldLandmarks = MutableList(33) {
            Landmark.create(0f, 0f, 0f, Optional.of(0.9f), Optional.of(0.9f))
        }
        // Hips at y = 0.5m, z = 0m
        worldLandmarks[23] = Landmark.create(-0.15f, 0.5f, 0f, Optional.of(0.9f), Optional.of(0.9f))
        worldLandmarks[24] = Landmark.create(0.15f, 0.5f, 0f, Optional.of(0.9f), Optional.of(0.9f))

        // Shoulders leaning backward (dz < 0): y = 0.0m, z = -0.15m
        worldLandmarks[11] = Landmark.create(-0.2f, 0.0f, -0.15f, Optional.of(0.9f), Optional.of(0.9f))
        worldLandmarks[12] = Landmark.create(0.2f, 0.0f, -0.15f, Optional.of(0.9f), Optional.of(0.9f))

        val result = mockPoseResult(worldLandmarks = listOf(worldLandmarks))
        val signal = extractor.extract(result, 1000L)
        assertTrue(signal.poseDetected)
        assertTrue("Lean angle should be negative for backward lean", signal.torsoLeanDeg!! < 0f)
    }

    @Test
    fun `shoulder tilt computed correctly`() {
        val worldLandmarks = MutableList(33) {
            Landmark.create(0f, 0f, 0f, Optional.of(0.9f), Optional.of(0.9f))
        }
        // Left shoulder at (-0.2, 0.0, 0), Right shoulder at (0.2, 0.1, 0) -> dy = 0.1, dx = 0.4
        worldLandmarks[11] = Landmark.create(-0.2f, 0.0f, 0f, Optional.of(0.9f), Optional.of(0.9f))
        worldLandmarks[12] = Landmark.create(0.2f, 0.1f, 0f, Optional.of(0.9f), Optional.of(0.9f))
        worldLandmarks[23] = Landmark.create(-0.15f, 0.5f, 0f, Optional.of(0.9f), Optional.of(0.9f))
        worldLandmarks[24] = Landmark.create(0.15f, 0.5f, 0f, Optional.of(0.9f), Optional.of(0.9f))

        val result = mockPoseResult(worldLandmarks = listOf(worldLandmarks))
        val signal = extractor.extract(result, 1000L)
        assertTrue(signal.poseDetected)
        val expectedTilt = Math.toDegrees(atan2(0.1, 0.4)).toFloat()
        assertEquals(expectedTilt, signal.shoulderTiltDeg!!, 0.5f)
    }

    @Test
    fun `reset clears movement state`() {
        val worldLandmarks = MutableList(33) {
            Landmark.create(0f, 0f, 0f, Optional.of(0.9f), Optional.of(0.9f))
        }
        val result = mockPoseResult(worldLandmarks = listOf(worldLandmarks))
        extractor.extract(result, 1000L)
        extractor.reset()
        val signal = extractor.extract(result, 2000L)
        assertEquals(0f, signal.movementScore!!, 0.01f)
    }
}
