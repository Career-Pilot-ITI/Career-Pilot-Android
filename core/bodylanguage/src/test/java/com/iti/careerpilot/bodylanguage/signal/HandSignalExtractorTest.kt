package com.iti.careerpilot.bodylanguage.signal

import com.google.mediapipe.tasks.components.containers.Category
import com.google.mediapipe.tasks.components.containers.Landmark
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class HandSignalExtractorTest {

    private lateinit var extractor: HandSignalExtractor

    @Before
    fun setup() {
        extractor = HandSignalExtractor()
    }

    private fun mockHandResult(
        landmarks: List<List<NormalizedLandmark>> = emptyList(),
        handednesses: List<List<Category>> = emptyList(),
        worldLandmarks: List<List<Landmark>> = emptyList(),
        timestampMs: Long = 0L,
    ): HandLandmarkerResult {
        return object : HandLandmarkerResult() {
            override fun landmarks(): List<List<NormalizedLandmark>> = landmarks
            override fun handedness(): List<List<Category>> = handednesses
            override fun worldLandmarks(): List<List<Landmark>> = worldLandmarks
            override fun timestampMs(): Long = timestampMs
        }
    }

    @Test
    fun `empty landmarks returns handsVisible 0 and null movement`() {
        val result = mockHandResult()
        val signal = extractor.extract(result, 1000L, 0.5f to 0.5f)
        assertEquals(0, signal.handsVisible)
        assertFalse(signal.handToFaceTouch)
        assertNull(signal.handMovementScore)
    }

    @Test
    fun `hand near face center detects touch`() {
        val handLandmarks = MutableList(21) {
            NormalizedLandmark.create(0.9f, 0.9f, 0f)
        }
        // Wrist (0) near face center (0.5, 0.5)
        handLandmarks[0] = NormalizedLandmark.create(0.52f, 0.52f, 0f)

        val result = mockHandResult(
            landmarks = listOf(handLandmarks),
            handednesses = listOf(listOf(Category.create(0.9f, 0, "Right", "Right"))),
            timestampMs = 1000L
        )
        val signal = extractor.extract(result, 1000L, 0.5f to 0.5f)
        assertTrue(signal.handToFaceTouch)
    }

    @Test
    fun `hand far from face center detects no touch`() {
        val handLandmarks = MutableList(21) {
            NormalizedLandmark.create(0.9f, 0.9f, 0f)
        }
        val result = mockHandResult(
            landmarks = listOf(handLandmarks),
            handednesses = listOf(listOf(Category.create(0.9f, 0, "Right", "Right"))),
            timestampMs = 1000L
        )
        val signal = extractor.extract(result, 1000L, 0.5f to 0.5f)
        assertFalse(signal.handToFaceTouch)
    }

    @Test
    fun `handedness tracking isolates left and right hand displacements`() {
        fun makeHand(wristX: Float, wristY: Float): List<NormalizedLandmark> {
            val list = MutableList(21) { NormalizedLandmark.create(0f, 0f, 0f) }
            list[0] = NormalizedLandmark.create(wristX, wristY, 0f)
            return list
        }

        // Frame 1: Left at (0.2, 0.5), Right at (0.8, 0.5)
        val frame1 = mockHandResult(
            landmarks = listOf(makeHand(0.2f, 0.5f), makeHand(0.8f, 0.5f)),
            handednesses = listOf(
                listOf(Category.create(0.9f, 0, "Left", "Left")),
                listOf(Category.create(0.9f, 1, "Right", "Right")),
            ),
            timestampMs = 1000L
        )
        extractor.extract(frame1, 1000L, null)

        // Frame 2: Left moves to (0.23, 0.5) [dx = 0.03], Right stays at (0.8, 0.5) [dx = 0]
        val frame2 = mockHandResult(
            landmarks = listOf(makeHand(0.23f, 0.5f), makeHand(0.8f, 0.5f)),
            handednesses = listOf(
                listOf(Category.create(0.9f, 0, "Left", "Left")),
                listOf(Category.create(0.9f, 1, "Right", "Right")),
            ),
            timestampMs = 1033L
        )
        val signal2 = extractor.extract(frame2, 1033L, null)
        // Average displacement = (0.03 + 0.0) / 2 = 0.015
        // Movement score = 0.015 / 0.06 = 0.25
        assertEquals(0.25f, signal2.handMovementScore!!, 0.01f)
    }

    @Test
    fun `handedness tracking prevents spurious displacement spikes when hand visibility alternates`() {
        fun makeHand(wristX: Float, wristY: Float): List<NormalizedLandmark> {
            val list = MutableList(21) { NormalizedLandmark.create(0f, 0f, 0f) }
            list[0] = NormalizedLandmark.create(wristX, wristY, 0f)
            return list
        }

        // Frame 1: Both Left (0.2, 0.5) and Right (0.8, 0.5) visible
        val frame1 = mockHandResult(
            landmarks = listOf(makeHand(0.2f, 0.5f), makeHand(0.8f, 0.5f)),
            handednesses = listOf(
                listOf(Category.create(0.9f, 0, "Left", "Left")),
                listOf(Category.create(0.9f, 1, "Right", "Right")),
            ),
            timestampMs = 1000L
        )
        extractor.extract(frame1, 1000L, null)

        // Frame 2: Left drops out, only Right is visible at (0.81, 0.5)
        val frame2 = mockHandResult(
            landmarks = listOf(makeHand(0.81f, 0.5f)),
            handednesses = listOf(
                listOf(Category.create(0.9f, 1, "Right", "Right")),
            ),
            timestampMs = 1033L
        )
        val signal2 = extractor.extract(frame2, 1033L, null)
        // Displacement = 0.01 for Right hand -> score = 0.01 / 0.06 ≈ 0.167
        assertEquals(0.167f, signal2.handMovementScore!!, 0.02f)

        // Frame 3: Right drops out, Left reappears at (0.2, 0.5)
        // Without handedness tracking, this would pair Left (0.2) with previous Right (0.81), giving dx = 0.61 (spike!)
        val frame3 = mockHandResult(
            landmarks = listOf(makeHand(0.2f, 0.5f)),
            handednesses = listOf(
                listOf(Category.create(0.9f, 0, "Left", "Left")),
            ),
            timestampMs = 1066L
        )
        val signal3 = extractor.extract(frame3, 1066L, null)
        // Because Left was not in frame 2, no previous position -> score = 0f (no spike)
        assertEquals(0f, signal3.handMovementScore!!, 0.01f)
    }

    @Test
    fun `reset clears previous wrist positions`() {
        fun makeHand(wristX: Float, wristY: Float): List<NormalizedLandmark> {
            val list = MutableList(21) { NormalizedLandmark.create(0f, 0f, 0f) }
            list[0] = NormalizedLandmark.create(wristX, wristY, 0f)
            return list
        }

        val frame = mockHandResult(
            landmarks = listOf(makeHand(0.2f, 0.5f)),
            handednesses = listOf(listOf(Category.create(0.9f, 0, "Left", "Left"))),
            timestampMs = 1000L
        )
        extractor.extract(frame, 1000L, null)
        extractor.reset()
        val signal = extractor.extract(frame, 2000L, null)
        assertEquals(0f, signal.handMovementScore!!, 0.01f)
    }
}
