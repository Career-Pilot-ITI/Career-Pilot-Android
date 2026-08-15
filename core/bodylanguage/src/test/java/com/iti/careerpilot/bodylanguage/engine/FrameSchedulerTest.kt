package com.iti.careerpilot.bodylanguage.engine

import com.google.mediapipe.framework.image.MPImage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FrameSchedulerTest {

    private class FakeFaceEngine : FaceLandmarkerEngine(context = null, onResult = { _, _ -> }) {
        val detectedTimestamps = mutableListOf<Long>()
        override fun detectAsync(image: MPImage, timestampMs: Long) {
            detectedTimestamps.add(timestampMs)
        }
    }

    private class FakePoseEngine : PoseLandmarkerEngine(context = null, onResult = { _, _ -> }) {
        val detectedTimestamps = mutableListOf<Long>()
        override fun detectAsync(image: MPImage, timestampMs: Long) {
            detectedTimestamps.add(timestampMs)
        }
    }

    private class FakeHandEngine : HandLandmarkerEngine(context = null, onResult = { _, _ -> }) {
        val detectedTimestamps = mutableListOf<Long>()
        override fun detectAsync(image: MPImage, timestampMs: Long) {
            detectedTimestamps.add(timestampMs)
        }
    }

    private fun dummyImage(): MPImage {
        val field = sun.misc.Unsafe::class.java.getDeclaredField("theUnsafe")
        field.isAccessible = true
        val unsafe = field.get(null) as sun.misc.Unsafe
        return unsafe.allocateInstance(MPImage::class.java) as MPImage
    }

    @Test
    fun `onFrame throttles face pose and hand calls according to fps intervals`() {
        val faceEngine = FakeFaceEngine()
        val poseEngine = FakePoseEngine()
        val handEngine = FakeHandEngine()

        val scheduler = FrameScheduler(
            faceEngine = faceEngine,
            poseEngine = poseEngine,
            handEngine = handEngine,
            faceFps = 8,  // 125ms interval
            poseFps = 4,  // 250ms interval
            handFps = 4,  // 250ms interval
        )

        val image = dummyImage()

        // t = 0ms: Initial frame. All 3 should trigger
        scheduler.onFrame(image, 0L)
        assertEquals(listOf(0L), faceEngine.detectedTimestamps)
        assertEquals(listOf(0L), poseEngine.detectedTimestamps)
        assertEquals(listOf(0L), handEngine.detectedTimestamps)

        // t = 100ms: < 125ms (face) and < 250ms (pose/hand). None should trigger.
        scheduler.onFrame(image, 100L)
        assertEquals(listOf(0L), faceEngine.detectedTimestamps)
        assertEquals(listOf(0L), poseEngine.detectedTimestamps)
        assertEquals(listOf(0L), handEngine.detectedTimestamps)

        // t = 130ms: >= 125ms for face, but < 250ms for pose/hand. Only face triggers.
        scheduler.onFrame(image, 130L)
        assertEquals(listOf(0L, 130L), faceEngine.detectedTimestamps)
        assertEquals(listOf(0L), poseEngine.detectedTimestamps)
        assertEquals(listOf(0L), handEngine.detectedTimestamps)

        // t = 260ms: >= 250ms for pose/hand, >= 125ms from last face (130ms). All trigger.
        scheduler.onFrame(image, 260L)
        assertEquals(listOf(0L, 130L, 260L), faceEngine.detectedTimestamps)
        assertEquals(listOf(0L, 260L), poseEngine.detectedTimestamps)
        assertEquals(listOf(0L, 260L), handEngine.detectedTimestamps)
    }

    @Test
    fun `shouldProcessFrame returns true for initial frame`() {
        val faceEngine = FakeFaceEngine()
        val scheduler = FrameScheduler(
            faceEngine = faceEngine,
            faceFps = 8,
        )

        assertTrue(scheduler.shouldProcessFrame(0L))
    }

    @Test
    fun `shouldProcessFrame returns false within interval and true when interval reached`() {
        val faceEngine = FakeFaceEngine()
        val poseEngine = FakePoseEngine()
        val handEngine = FakeHandEngine()

        val scheduler = FrameScheduler(
            faceEngine = faceEngine,
            poseEngine = poseEngine,
            handEngine = handEngine,
            faceFps = 8,  // 125ms interval
            poseFps = 4,  // 250ms interval
            handFps = 4,  // 250ms interval
        )

        val image = dummyImage()

        // Process first frame at t = 0ms
        scheduler.onFrame(image, 0L)

        // At t = 100ms (< 125ms for face, < 250ms for pose/hand), should process frame is false
        assertFalse(scheduler.shouldProcessFrame(100L))

        // At t = 125ms (>= 125ms for face), should process frame is true
        assertTrue(scheduler.shouldProcessFrame(125L))
    }

    @Test
    fun `shouldProcessFrame returns true when pose or hand interval is reached`() {
        val faceEngine = FakeFaceEngine()
        val poseEngine = FakePoseEngine()

        val scheduler = FrameScheduler(
            faceEngine = faceEngine,
            poseEngine = poseEngine,
            faceFps = 8,  // 125ms interval
            poseFps = 4,  // 250ms interval
        )

        val image = dummyImage()

        // Initial frame processed at t = 0ms for face & pose
        scheduler.onFrame(image, 0L)

        // Frame processed at t = 200ms for face (face lastMs becomes 200L, pose lastMs remains 0L)
        scheduler.onFrame(image, 200L)

        // At t = 260ms:
        // face diff = 260 - 200 = 60ms (< 125ms) -> face not due
        // pose diff = 260 - 0 = 260ms (>= 250ms) -> pose IS due!
        assertTrue(scheduler.shouldProcessFrame(260L))
    }

    @Test
    fun `reset clears last timestamp markers`() {
        val faceEngine = FakeFaceEngine()
        val poseEngine = FakePoseEngine()
        val handEngine = FakeHandEngine()

        val scheduler = FrameScheduler(
            faceEngine = faceEngine,
            poseEngine = poseEngine,
            handEngine = handEngine,
            faceFps = 8,
            poseFps = 4,
            handFps = 4,
        )

        val image = dummyImage()

        scheduler.onFrame(image, 1000L)
        scheduler.reset()

        // After reset, shouldProcessFrame and onFrame for t = 100ms should trigger again
        assertTrue(scheduler.shouldProcessFrame(100L))
        scheduler.onFrame(image, 100L)
        assertEquals(listOf(1000L, 100L), faceEngine.detectedTimestamps)
        assertEquals(listOf(1000L, 100L), poseEngine.detectedTimestamps)
        assertEquals(listOf(1000L, 100L), handEngine.detectedTimestamps)
    }
}
