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

    val subscriptionTier: String = "",
    val scoreSummary: ScoreSummary? = null,

    val availableInterviews: ImmutableList<InterviewTrack> = persistentListOf(),
    val recentSessions: ImmutableList<InterviewSession> = persistentListOf(),
) {
    val canStartPractice: Boolean get() = practiceTrackId != null

    val isSubscribed: Boolean
        get() = subscriptionTier.uppercase() in PAID_TIERS

    val planLabel: String
        get() = when (subscriptionTier.uppercase()) {
            "PLUS" -> "Plus"
            "PRO" -> "Pro"
            "MAX" -> "Max"
            else -> "Free"
        }

    private companion object {
        val PAID_TIERS = setOf("PLUS", "PRO", "MAX")
    }
}
