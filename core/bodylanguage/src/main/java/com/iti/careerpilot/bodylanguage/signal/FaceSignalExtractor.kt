package com.iti.careerpilot.bodylanguage.signal

import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarkerResult
import com.iti.careerpilot.bodylanguage.model.FaceFrameSignal
import javax.inject.Inject

internal class FaceSignalExtractor @Inject constructor(
    private val eyeContactApproximator: EyeContactApproximator,
) {

    fun extract(result: FaceLandmarkerResult, timestampMs: Long): FaceFrameSignal {
        if (result.faceLandmarks().isEmpty()) {
            return FaceFrameSignal(
                timestampMs = timestampMs,
                faceDetected = false,
                smileScore = null,
                headYawDeg = null,
                headPitchDeg = null,
                headRollDeg = null,
                lookingAtCamera = null,
                leftEyeOpenScore = null,
                rightEyeOpenScore = null,
            )
        }

        val blendshapes = result.faceBlendshapes()
            .orElse(null)
            ?.firstOrNull()
            ?.associate { it.categoryName() to it.score() }
            ?: emptyMap()

        val matrix = result.facialTransformationMatrixes()
            .orElse(null)
            ?.firstOrNull()

        val headYaw = matrix?.let { extractYaw(it) }
        val headPitch = matrix?.let { extractPitch(it) }
        val headRoll = matrix?.let { extractRoll(it) }

        val smileLeft = blendshapes["mouthSmileLeft"] ?: 0f
        val smileRight = blendshapes["mouthSmileRight"] ?: 0f
        val smileScore = (smileLeft + smileRight) / 2f

        val leftEyeOpen = blendshapes["eyeBlinkLeft"]?.let { 1f - it }
        val rightEyeOpen = blendshapes["eyeBlinkRight"]?.let { 1f - it }

        val lookingAtCamera = eyeContactApproximator.isLookingAtCamera(
            headYawDeg = headYaw,
            headPitchDeg = headPitch,
            landmarks = result.faceLandmarks().firstOrNull(),
        )

        return FaceFrameSignal(
            timestampMs = timestampMs,
            faceDetected = true,
            smileScore = smileScore,
            headYawDeg = headYaw,
            headPitchDeg = headPitch,
            headRollDeg = headRoll,
            lookingAtCamera = lookingAtCamera,
            leftEyeOpenScore = leftEyeOpen,
            rightEyeOpenScore = rightEyeOpen,
        )
    }

    /**
     * Extracts Euler angles from the 4×4 facial transformation matrix.
     * Matrix is column-major float array of length 16.
     */
    private fun extractYaw(matrix: FloatArray): Float? {
        if (matrix.size < 16) return null
        return Math.toDegrees(
            kotlin.math.atan2(matrix[8].toDouble(), matrix[10].toDouble())
        ).toFloat()
    }

    private fun extractPitch(matrix: FloatArray): Float? {
        if (matrix.size < 16) return null
        return Math.toDegrees(
            kotlin.math.asin((-matrix[9]).toDouble().coerceIn(-1.0, 1.0))
        ).toFloat()
    }

    private fun extractRoll(matrix: FloatArray): Float? {
        if (matrix.size < 16) return null
        return Math.toDegrees(
            kotlin.math.atan2(matrix[1].toDouble(), matrix[0].toDouble())
        ).toFloat()
    }
}
