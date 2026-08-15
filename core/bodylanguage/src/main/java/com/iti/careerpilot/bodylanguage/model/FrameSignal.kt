package com.iti.careerpilot.bodylanguage.model

/**
 * Lightweight per-frame signals extracted from MediaPipe landmarks.
 * These are internal — never cross the module boundary.
 * Each field is nullable to represent "below confidence threshold."
 */
internal data class FaceFrameSignal(
    val timestampMs: Long,
    val faceDetected: Boolean,
    val smileScore: Float?,
    val headYawDeg: Float?,
    val headPitchDeg: Float?,
    val headRollDeg: Float?,
    val lookingAtCamera: Boolean?,
    val leftEyeOpenScore: Float?,
    val rightEyeOpenScore: Float?,
)

internal data class PostureFrameSignal(
    val timestampMs: Long,
    val poseDetected: Boolean,
    val torsoLeanDeg: Float?,
    val shoulderTiltDeg: Float?,
    val slouchScore: Float?,
    val movementScore: Float?,
)

internal data class HandFrameSignal(
    val timestampMs: Long,
    val handsVisible: Int,
    val handToFaceTouch: Boolean,
    val handMovementScore: Float?,
)
