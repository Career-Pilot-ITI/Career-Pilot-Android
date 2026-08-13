package com.iti.careerpilot.ats.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CvOptimizationDto(
    @SerialName("sections") val sections: List<CvOptimizationSectionDto> = emptyList(),
    @SerialName("recommendedTracks") val recommendedTracks: List<String> = emptyList(),
    @SerialName("coinCost") val coinCost: Int? = null,
)

@Serializable
data class CvOptimizationSectionDto(
    @SerialName("name") val name: String? = null,
    @SerialName("score") val score: Int = 0,
    @SerialName("improvements") val improvements: List<CvSectionImprovementDto> = emptyList(),
)

@Serializable
data class CvSectionImprovementDto(
    @SerialName("original") val original: String? = null,
    @SerialName("improved") val improved: String? = null,
    @SerialName("reason") val reason: String? = null,
)
