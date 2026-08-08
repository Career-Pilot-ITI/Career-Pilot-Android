package com.iti.careerpilot.bodylanguage.engine

import com.google.mediapipe.framework.image.MPImage
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test

class FrameSchedulerTest {

    @Test
    fun `onFrame throttles face, pose, and hand calls according to fps intervals`() {
        val faceEngine = mockk<FaceLandmarkerEngine>(relaxed = true)
        val poseEngine = mockk<PoseLandmarkerEngine>(relaxed = true)
        val handEngine = mockk<HandLandmarkerEngine>(relaxed = true)
        val image = mockk<MPImage>(relaxed = true)

        val scheduler = FrameScheduler(
            faceEngine = faceEngine,
            poseEngine = poseEngine,
            handEngine = handEngine,
            faceFps = 8,  // 125ms interval
            poseFps = 4,  // 250ms interval
            handFps = 4,  // 250ms interval
        )

        // t = 0ms: Initial frame. All 3 should trigger
        scheduler.onFrame(image, 0L)
        verify(exactly = 1) { faceEngine.detectAsync(image, 0L) }
        verify(exactly = 1) { poseEngine.detectAsync(image, 0L) }
        verify(exactly = 1) { handEngine.detectAsync(image, 0L) }

        // t = 100ms: < 125ms (face) and < 250ms (pose/hand). None should trigger.
        scheduler.onFrame(image, 100L)
        verify(exactly = 1) { faceEngine.detectAsync(any(), any()) }
        verify(exactly = 1) { poseEngine.detectAsync(any(), any()) }
        verify(exactly = 1) { handEngine.detectAsync(any(), any()) }

        // t = 130ms: >= 125ms for face, but < 250ms for pose/hand. Only face triggers.
        scheduler.onFrame(image, 130L)
        verify(exactly = 2) { faceEngine.detectAsync(any(), any()) }
        verify(exactly = 1) { poseEngine.detectAsync(any(), any()) }
        verify(exactly = 1) { handEngine.detectAsync(any(), any()) }

        // t = 260ms: >= 250ms for pose/hand, >= 125ms from last face (130ms). All trigger.
        scheduler.onFrame(image, 260L)
        verify(exactly = 3) { faceEngine.detectAsync(any(), any()) }
        verify(exactly = 2) { poseEngine.detectAsync(any(), any()) }
        verify(exactly = 2) { handEngine.detectAsync(any(), any()) }
    }

    @Test
    fun `reset clears last timestamp markers`() {
        val faceEngine = mockk<FaceLandmarkerEngine>(relaxed = true)
        val poseEngine = mockk<PoseLandmarkerEngine>(relaxed = true)
        val handEngine = mockk<HandLandmarkerEngine>(relaxed = true)
        val image = mockk<MPImage>(relaxed = true)

        val scheduler = FrameScheduler(
            faceEngine = faceEngine,
            poseEngine = poseEngine,
            handEngine = handEngine,
            faceFps = 8,
            poseFps = 4,
            handFps = 4,
        )

        scheduler.onFrame(image, 1000L)
        scheduler.reset()

        // After reset, t = 100ms should trigger again because last timestamp was reset to 0L
        scheduler.onFrame(image, 100L)
        verify(exactly = 2) { faceEngine.detectAsync(any(), any()) }
        verify(exactly = 2) { poseEngine.detectAsync(any(), any()) }
        verify(exactly = 2) { handEngine.detectAsync(any(), any()) }
    }
}
