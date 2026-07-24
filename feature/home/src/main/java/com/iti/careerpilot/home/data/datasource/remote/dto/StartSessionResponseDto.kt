package com.iti.careerpilot.home.data.datasource.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StartSessionResponseDto(
    @SerialName("sessionId") val sessionId: Long? = null,
    @SerialName("trackName") val trackName: String? = null,
    @SerialName("targetDurationMinutes") val targetDurationMinutes: Int? = null,
    @SerialName("maxQuestions") val maxQuestions: Int? = null,
    @SerialName("startedAt") val startedAt: String? = null,
)
