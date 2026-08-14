package com.iti.careerpilot.ats.domain.model

import kotlinx.collections.immutable.ImmutableList

data class AtsScore(
    val overallScore: Int,
    val matchPercentage: Int,
    val matchedSkills: ImmutableList<String>,
    val missingRequiredSkills: ImmutableList<String>,
    val missingPreferredSkills: ImmutableList<String>,
    val strengths: ImmutableList<String>,
    val weaknesses: ImmutableList<String>,
    val sections: ImmutableList<AtsSectionScore>,
    val recommendations: ImmutableList<String>,
    val coinCost: Int?,
    val cvScoreUpdatedAt: String?,
)

data class AtsSectionScore(
    val section: String,
    val score: Int,
    val feedback: String,
)
