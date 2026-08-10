package com.iti.careerpilot.ats.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CoverLetterDto(
    @SerialName("coverLetter") val coverLetter: String? = null,
    @SerialName("approachTips") val approachTips: String? = null,
    @SerialName("coinCost") val coinCost: Int? = null,
)
