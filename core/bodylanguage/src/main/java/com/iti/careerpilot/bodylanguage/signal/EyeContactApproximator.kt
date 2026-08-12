package com.iti.careerpilot.bodylanguage.signal

import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import javax.inject.Inject
import kotlin.math.abs

/**
 * Approximates whether the user is looking at the camera.
 *
 * NOT a true 3D gaze tracker — uses head pose + iris position as proxy.
 * Reliable for laptop/phone-distance interview setup where the camera is
 * near the screen. Will misfire for eye-only glances without head movement.
 */
internal class EyeContactApproximator @Inject constructor() {

    companion object {
        private const val YAW_THRESHOLD_DEG = 18f
        private const val PITCH_THRESHOLD_DEG = 14f

        // MediaPipe Face Mesh iris landmark indices
        private const val LEFT_IRIS_CENTER = 468
        private const val RIGHT_IRIS_CENTER = 473
        private const val LEFT_EYE_INNER = 133
        private const val LEFT_EYE_OUTER = 33
        private const val RIGHT_EYE_INNER = 362
        private const val RIGHT_EYE_OUTER = 263

        private const val IRIS_OFFSET_THRESHOLD = 0.28f
    }

    fun isLookingAtCamera(
        headYawDeg: Float?,
        headPitchDeg: Float?,
        landmarks: List<NormalizedLandmark>?,
    ): Boolean? {
        // If head pose is available, verify head is roughly facing the camera
        if (headYawDeg != null && headPitchDeg != null) {
            if (abs(headYawDeg) > YAW_THRESHOLD_DEG || abs(headPitchDeg) > PITCH_THRESHOLD_DEG) {
                return false
            }
        }

        // If we have iris landmarks, check iris centering
        if (landmarks != null && landmarks.size > RIGHT_IRIS_CENTER) {
            val leftIrisCentered = isIrisCentered(
                iris = landmarks[LEFT_IRIS_CENTER],
                eyeInner = landmarks[LEFT_EYE_INNER],
                eyeOuter = landmarks[LEFT_EYE_OUTER],
            )
            val rightIrisCentered = isIrisCentered(
                iris = landmarks[RIGHT_IRIS_CENTER],
                eyeInner = landmarks[RIGHT_EYE_INNER],
                eyeOuter = landmarks[RIGHT_EYE_OUTER],
            )
            return leftIrisCentered && rightIrisCentered
        }

        // Fallback: if head is facing camera, treat as looking at camera
        return if (headYawDeg != null && headPitchDeg != null) true else null
    }

    private fun isIrisCentered(
        iris: NormalizedLandmark,
        eyeInner: NormalizedLandmark,
        eyeOuter: NormalizedLandmark,
    ): Boolean {
        val eyeCenterX = (eyeInner.x() + eyeOuter.x()) / 2f
        val eyeWidth = abs(eyeInner.x() - eyeOuter.x())
        if (eyeWidth < 0.001f) return true // Avoid division by near-zero
        val normalizedOffset = abs(iris.x() - eyeCenterX) / eyeWidth
        return normalizedOffset < IRIS_OFFSET_THRESHOLD
    }
}
