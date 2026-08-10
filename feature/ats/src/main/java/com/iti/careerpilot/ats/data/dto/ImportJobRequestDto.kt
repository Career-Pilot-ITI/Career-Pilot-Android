package com.iti.careerpilot.ats.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ImportJobRequestDto(
    @SerialName("url") val url: String,
)
