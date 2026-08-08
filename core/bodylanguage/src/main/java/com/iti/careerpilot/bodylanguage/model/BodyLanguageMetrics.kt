package com.iti.careerpilot.bodylanguage.model

import kotlinx.serialization.Serializable

@Serializable
data class BodyLanguageMetrics(
    val schemaVersion: Int = 1,
    val sessionDurationMs: Long,

    // Face
    val averageSmile: Float,
    val maxSmile: Float,
    val eyeContactPercentage: Float,
    val timeLookingAwayMs: Long,
    val faceLostCount: Int,

    // Posture
    val averageTorsoLeanDeg: Float,
    val averageShoulderTiltDeg: Float,
    val slouchPercentage: Float,
    val postureChanges: Int,

    // Hands
    val handsVisiblePercentage: Float,
    val handToFaceTouchCount: Int,
    val fidgetScore: Float,

    val keyMoments: List<KeyMoment>,
) {
    companion object {
        val EMPTY = BodyLanguageMetrics(
            sessionDurationMs = 0L,
            averageSmile = 0f,
            maxSmile = 0f,
            eyeContactPercentage = 0f,
            timeLookingAwayMs = 0L,
            faceLostCount = 0,
            averageTorsoLeanDeg = 0f,
            averageShoulderTiltDeg = 0f,
            slouchPercentage = 0f,
            postureChanges = 0,
            handsVisiblePercentage = 0f,
            handToFaceTouchCount = 0,
            fidgetScore = 0f,
            keyMoments = emptyList(),
        )
    }
}
