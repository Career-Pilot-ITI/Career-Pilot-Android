package com.iti.careerpilot.ats.domain.model

import kotlinx.collections.immutable.ImmutableList

data class CvOptimization(
    val sections: ImmutableList<CvOptimizationSection>,
    val recommendedTracks: ImmutableList<String>,
    val coinCost: Int?,
)

data class CvOptimizationSection(
    val name: String,
    val score: Int,
    val improvements: ImmutableList<CvSectionImprovement>,
)

data class CvSectionImprovement(
    val original: String,
    val improved: String,
    val reason: String,
)
