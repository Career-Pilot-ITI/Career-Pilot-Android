package com.iti.careerpilot.bodylanguage.signal

import com.google.mediapipe.tasks.components.containers.Landmark
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult
import com.iti.careerpilot.bodylanguage.model.PostureFrameSignal
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.sqrt

/**
 * Extracts posture signals from PoseLandmarker world landmarks.
 * Uses WORLD landmarks (real-world 3D meters, hip-centered), not image landmarks,
 * since image coordinates distort with camera distance.
 */
internal class PostureSignalExtractor @Inject constructor() {

    companion object {
        // Pose landmark indices
        private const val LEFT_SHOULDER = 11
        private const val RIGHT_SHOULDER = 12
        private const val LEFT_HIP = 23
        private const val RIGHT_HIP = 24
        private const val LEFT_EAR = 7
        private const val RIGHT_EAR = 8

        private const val MIN_VISIBILITY = 0.5f
    }

    private var previousShoulderMidX: Float? = null
    private var previousShoulderMidY: Float? = null

    fun extract(result: PoseLandmarkerResult, timestampMs: Long): PostureFrameSignal {
        val worldLandmarks = result.worldLandmarks()
        if (worldLandmarks.isEmpty()) {
            return PostureFrameSignal(
                timestampMs = timestampMs,
                poseDetected = false,
                torsoLeanDeg = null,
                shoulderTiltDeg = null,
                slouchScore = null,
                movementScore = null,
            )
        }

        val landmarks = worldLandmarks.first()
        val ls = landmarks[LEFT_SHOULDER]
        val rs = landmarks[RIGHT_SHOULDER]

        // Check visibility
        if (ls.visibility().orElse(0f) < MIN_VISIBILITY ||
            rs.visibility().orElse(0f) < MIN_VISIBILITY
        ) {
            return PostureFrameSignal(
                timestampMs = timestampMs,
                poseDetected = false,
                torsoLeanDeg = null,
                shoulderTiltDeg = null,
                slouchScore = null,
                movementScore = null,
            )
        }

        val torsoLean = computeTorsoLeanDeg(landmarks)
        val shoulderTilt = computeShoulderTiltDeg(ls, rs)
        val slouch = computeSlouchScore(landmarks, torsoLean)
        val movement = computeMovementScore(ls, rs)

        return PostureFrameSignal(
            timestampMs = timestampMs,
            poseDetected = true,
            torsoLeanDeg = torsoLean,
            shoulderTiltDeg = shoulderTilt,
            slouchScore = slouch,
            movementScore = movement,
        )
    }

    /**
     * Angle between vector (mid-hip → mid-shoulder) and vertical in sagittal plane (Z-Y).
     * Positive = leaning forward, negative = leaning back.
     */
    private fun computeTorsoLeanDeg(landmarks: List<Landmark>): Float {
        val midHipZ = (landmarks[LEFT_HIP].z() + landmarks[RIGHT_HIP].z()) / 2f
        val midHipY = (landmarks[LEFT_HIP].y() + landmarks[RIGHT_HIP].y()) / 2f
        val midShoulderZ = (landmarks[LEFT_SHOULDER].z() + landmarks[RIGHT_SHOULDER].z()) / 2f
        val midShoulderY = (landmarks[LEFT_SHOULDER].y() + landmarks[RIGHT_SHOULDER].y()) / 2f
        val dz = midShoulderZ - midHipZ
        val dy = midShoulderY - midHipY
        return Math.toDegrees(atan2(dz.toDouble(), (-dy).toDouble())).toFloat()
    }

    /**
     * Angle of shoulder line relative to horizontal. 0 = level.
     */
    private fun computeShoulderTiltDeg(ls: Landmark, rs: Landmark): Float {
        val dx = rs.x() - ls.x()
        val dy = rs.y() - ls.y()
        return Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
    }

    /**
     * Heuristic slouch score (0-1):
     * Combines forward lean + reduced ear-to-shoulder vertical distance.
     */
    private fun computeSlouchScore(landmarks: List<Landmark>, torsoLeanDeg: Float): Float {
        val leanComponent = (abs(torsoLeanDeg) / 30f).coerceIn(0f, 1f)

        val earMidY = (landmarks[LEFT_EAR].y() + landmarks[RIGHT_EAR].y()) / 2f
        val shoulderMidY = (landmarks[LEFT_SHOULDER].y() + landmarks[RIGHT_SHOULDER].y()) / 2f
        val earShoulderDist = abs(earMidY - shoulderMidY)
        // Smaller distance = more slouch. Normalize assuming ~0.2m is good posture.
        val compressionComponent = (1f - (earShoulderDist / 0.2f)).coerceIn(0f, 1f)

        return (leanComponent * 0.6f + compressionComponent * 0.4f).coerceIn(0f, 1f)
    }

    /**
     * Frame-to-frame shoulder midpoint displacement, normalized.
     */
    private fun computeMovementScore(ls: Landmark, rs: Landmark): Float {
        val midX = (ls.x() + rs.x()) / 2f
        val midY = (ls.y() + rs.y()) / 2f

        val score = previousShoulderMidX?.let { prevX ->
            val prevY = previousShoulderMidY!!
            val dx = midX - prevX
            val dy = midY - prevY
            sqrt((dx * dx + dy * dy).toDouble()).toFloat()
        } ?: 0f

        previousShoulderMidX = midX
        previousShoulderMidY = midY

        // Normalize: typical small movement ~0.005m, fidgeting ~0.02m+
        return (score / 0.02f).coerceIn(0f, 1f)
    }

    fun reset() {
        previousShoulderMidX = null
        previousShoulderMidY = null
    }
}
