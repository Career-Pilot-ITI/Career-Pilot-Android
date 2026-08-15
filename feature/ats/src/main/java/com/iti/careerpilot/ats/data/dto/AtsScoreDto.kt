package com.iti.careerpilot.ats.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AtsScoreDto(
    @SerialName("overallScore") val overallScore: Int,
    @SerialName("matchPercentage") val matchPercentage: Int,
    @SerialName("matchedSkills") val matchedSkills: List<String> = emptyList(),
    @SerialName("missingRequiredSkills") val missingRequiredSkills: List<String> = emptyList(),
    @SerialName("missingPreferredSkills") val missingPreferredSkills: List<String> = emptyList(),
    @SerialName("strengths") val strengths: List<String> = emptyList(),
    @SerialName("weaknesses") val weaknesses: List<String> = emptyList(),
    @SerialName("sections") val sections: List<AtsSectionScoreDto> = emptyList(),
    @SerialName("recommendations") val recommendations: List<String> = emptyList(),
    @SerialName("coinCost") val coinCost: Int? = null,
    @SerialName("cvScoreUpdatedAt") val cvScoreUpdatedAt: String? = null,
)

@Serializable
data class AtsSectionScoreDto(
    @SerialName("section") val section: String? = null,
    @SerialName("score") val score: Int,
    @SerialName("feedback") val feedback: String? = null,
)
