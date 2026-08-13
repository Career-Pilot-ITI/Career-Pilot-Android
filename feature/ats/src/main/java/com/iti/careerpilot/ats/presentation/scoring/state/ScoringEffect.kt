package com.iti.careerpilot.ats.presentation.scoring.state

import com.iti.careerpilot.ats.domain.model.AiJob
import com.iti.common.util.UIText

sealed interface ScoringEffect {
    data object OpenCoinsPaywall : ScoringEffect
    data class OpenCoverLetter(val workspaceId: Long) : ScoringEffect
    data class StartOptimizationTracking(val job: AiJob) : ScoringEffect
    data class ShowMessage(val message: UIText) : ScoringEffect
    data class OpenPractice(
        val trackId: Long,
        val trackName: String,
        val workspaceId: Long,
    ) : ScoringEffect
}
