package com.iti.core.model.bodylanguage

import kotlinx.serialization.Serializable

@Serializable
enum class KeyMomentType {
    SMILE_PEAK,
    EYE_CONTACT_LOST,
    SLOUCH_START,
    HAND_FIDGET_SPIKE,
    FACE_LOST,
    POSTURE_SHIFT,
    HAND_TO_FACE_TOUCH,
}

@Serializable
data class KeyMoment(
    val timestampMs: Long,
    val type: KeyMomentType,
    val intensity: Float = 1.0f,
    val durationMs: Long = 0L,
)
