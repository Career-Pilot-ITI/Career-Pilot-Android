package com.iti.careerpilot.ats.domain.model

data class CvOptimization(
    val optimizedCv: String,
    val recommendedTracks: List<String>,
    val coinCost: Int?,
)
