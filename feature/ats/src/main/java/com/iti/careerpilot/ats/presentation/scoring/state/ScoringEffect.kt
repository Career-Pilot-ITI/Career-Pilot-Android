package com.iti.careerpilot.ats.presentation.scoring.state

sealed interface ScoringEffect {
    data object OpenCoinsPaywall : ScoringEffect
    data class OpenCoverLetter(val workspaceId: Long) : ScoringEffect
    data class OpenOptimizedCv(val workspaceId: Long) : ScoringEffect
    data class OpenPractice(val trackId: Long) : ScoringEffect
}
