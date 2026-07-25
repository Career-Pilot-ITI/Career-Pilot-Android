package com.iti.careerpilot.home.presentation.home

import com.iti.careerpilot.home.domain.model.InterviewSession
import com.iti.careerpilot.home.domain.model.InterviewTrack
import com.iti.careerpilot.home.domain.model.ScoreSummary
import com.iti.common.util.UIText
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class HomeState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val userName: String = "",

    val practiceTrackName: String = "",

    val practiceTrackId: Long? = null,

    val coins: Int? = null,

    val trial: TrialUiState? = null,
    val scoreSummary: ScoreSummary? = null,

    val availableInterviews: ImmutableList<InterviewTrack> = persistentListOf(),
    val recentSessions: ImmutableList<InterviewSession> = persistentListOf(),
    val error: UIText? = null,
) {
    val canStartPractice: Boolean get() = practiceTrackId != null
}

data class TrialUiState(
    val sessionsUsed: Int,
    val totalFreeSessions: Int,
) {
    val sessionsRemaining: Int get() = (totalFreeSessions - sessionsUsed).coerceAtLeast(0)
    val progress: Float
        get() = if (totalFreeSessions <= 0) 0f
        else (sessionsUsed.toFloat() / totalFreeSessions).coerceIn(0f, 1f)
}