package com.iti.careerpilot.ats.domain.model

data class CvOptimization(
    val sections: List<CvOptimizationSection>,
    val recommendedTracks: List<String>,
    val coinCost: Int?,
)

data class CvOptimizationSection(
    val name: String,
    val score: Int,
    val improvements: List<CvSectionImprovement>,
)

data class CvSectionImprovement(
    val original: String,
    val improved: String,
    val reason: String,
)
