package com.iti.careerpilot.ats.domain.model

data class AtsScore(
    val overallScore: Int,
    val matchPercentage: Int,
    val matchedSkills: List<String>,
    val missingRequiredSkills: List<String>,
    val missingPreferredSkills: List<String>,
    val strengths: List<String>,
    val weaknesses: List<String>,
    val sections: List<AtsSectionScore>,
    val recommendations: List<String>,
    val coinCost: Int?,
    val cvScoreUpdatedAt: String?,
)

data class AtsSectionScore(
    val section: String,
    val score: Int,
    val feedback: String,
)
