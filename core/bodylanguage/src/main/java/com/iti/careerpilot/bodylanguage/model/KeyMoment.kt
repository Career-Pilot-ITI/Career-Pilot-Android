package com.iti.careerpilot.bodylanguage.model

import kotlinx.serialization.Serializable

@Serializable
enum class KeyMomentType {
    LOOKED_AWAY,
    SLOUCHED,
    HAND_TO_FACE,
    FIDGETED,
    FACE_LOST,
    SMILED,
}

@Serializable
data class KeyMoment(
    val timestampMs: Long,
    val type: KeyMomentType,
    val durationMs: Long? = null,
)
