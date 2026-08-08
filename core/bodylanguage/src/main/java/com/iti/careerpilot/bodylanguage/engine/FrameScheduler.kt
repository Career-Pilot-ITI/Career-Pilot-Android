package com.iti.careerpilot.bodylanguage.engine

import com.google.mediapipe.framework.image.MPImage
import java.util.concurrent.atomic.AtomicLong

/**
 * Throttles frame dispatch to each MediaPipe task at independent fps.
 * Face: 8fps, Pose: 4fps, Hands: 4fps.
 *
 * Call [onFrame] from the CameraX ImageAnalysis callback on Dispatchers.Default.
 */
internal class FrameScheduler(
    private val faceEngine: FaceLandmarkerEngine,
    private val poseEngine: PoseLandmarkerEngine,
    private val handEngine: HandLandmarkerEngine,
    faceFps: Int = 8,
    poseFps: Int = 4,
    handFps: Int = 4,
) {
    private val faceIntervalMs = 1000L / faceFps
    private val poseIntervalMs = 1000L / poseFps
    private val handIntervalMs = 1000L / handFps

    private val lastFaceMs = AtomicLong(-1L)
    private val lastPoseMs = AtomicLong(-1L)
    private val lastHandMs = AtomicLong(-1L)

    fun onFrame(image: MPImage, timestampMs: Long) {
        val lastFace = lastFaceMs.get()
        if (lastFace == -1L || timestampMs - lastFace >= faceIntervalMs) {
            if (lastFaceMs.compareAndSet(lastFace, timestampMs)) {
                faceEngine.detectAsync(image, timestampMs)
            }
        }
        val lastPose = lastPoseMs.get()
        if (lastPose == -1L || timestampMs - lastPose >= poseIntervalMs) {
            if (lastPoseMs.compareAndSet(lastPose, timestampMs)) {
                poseEngine.detectAsync(image, timestampMs)
            }
        }
        val lastHand = lastHandMs.get()
        if (lastHand == -1L || timestampMs - lastHand >= handIntervalMs) {
            if (lastHandMs.compareAndSet(lastHand, timestampMs)) {
                handEngine.detectAsync(image, timestampMs)
            }
        }
    }

    fun reset() {
        lastFaceMs.set(-1L)
        lastPoseMs.set(-1L)
        lastHandMs.set(-1L)
    }
}
