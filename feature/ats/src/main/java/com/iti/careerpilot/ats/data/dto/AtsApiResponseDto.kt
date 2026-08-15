package com.iti.careerpilot.ats.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AtsApiResponseDto<T>(
    @SerialName("success") val success: Boolean = false,
    @SerialName("message") val message: String? = null,
    @SerialName("timestamp") val timestamp: String? = null,
    @SerialName("data") val data: T? = null,
)
