package com.iti.careerpilot.ats.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CvOptimizationDto(
    @SerialName("optimizedCv") val optimizedCv: String? = null,
    @SerialName("recommendedTracks") val recommendedTracks: List<String> = emptyList(),
    @SerialName("coinCost") val coinCost: Int? = null,
)
