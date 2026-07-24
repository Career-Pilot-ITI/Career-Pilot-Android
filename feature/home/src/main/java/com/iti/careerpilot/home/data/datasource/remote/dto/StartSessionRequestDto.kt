package com.iti.careerpilot.home.data.datasource.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StartSessionRequestDto(
    @SerialName("trackId") val trackId: Long,
    @SerialName("questionCount") val questionCount: Int? = null,
    @SerialName("durationMinutes") val durationMinutes: Int? = null,
)
