package com.iti.careerpilot.home.data.datasource.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiEnvelopeDto<T>(
    @SerialName("data") val data: T? = null,
    @SerialName("success") val success: Boolean = false,
    @SerialName("message") val message: String? = null,
)
