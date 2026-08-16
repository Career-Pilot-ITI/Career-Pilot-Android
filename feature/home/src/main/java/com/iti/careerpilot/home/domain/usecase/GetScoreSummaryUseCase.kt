package com.iti.careerpilot.home.domain.usecase

import com.iti.careerpilot.core.interviews.domain.model.InterviewSession
import com.iti.careerpilot.home.domain.model.ScoreSummary
import javax.inject.Inject

class GetScoreSummaryUseCase @Inject constructor() {

    operator fun invoke(sessions: List<InterviewSession>): ScoreSummary? {
        val scored = sessions.mapNotNull { session -> session.score }
        val latest = scored.firstOrNull() ?: return null
        val previous = scored.getOrNull(1)

        return ScoreSummary(
            latestScore = latest,
            deltaFromPrevious = previous?.let { latest - it },
        )
    }
}
