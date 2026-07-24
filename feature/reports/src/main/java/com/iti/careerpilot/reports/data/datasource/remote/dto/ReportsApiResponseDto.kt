package com.iti.careerpilot.reports.data.datasource.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ReportsApiResponseDto<T>(
    @SerialName("message")
    val message: String? = null,
    @SerialName("success")
    val success: Boolean = false,
    @SerialName("timestamp")
    val timestamp: String? = null,
    @SerialName("data")
    val data: T,
)
