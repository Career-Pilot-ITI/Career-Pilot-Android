package com.iti.careerpilot.features.home

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class HomeState(
    val userName: String = "",
    val coinBalance: Int = 0,
    val subscriptionTier: String = "FREE",
    val trialSessionsUsed: Int = 0,
    val totalTrialSessions: Int = 3,
    val overallScore: Int = 0,
    val scoreTrend: String = "",
    val scoreFeedback: String = "Keep practicing!",
    val recommendedSessions: ImmutableList<RecommendedSession> = persistentListOf(),
    val recentSessions: ImmutableList<RecentSession> = persistentListOf()
) {
    val formattedSubscriptionTier: String
        get() = when (subscriptionTier.uppercase()) {
            "PRO" -> "Pro"
            "MAX" -> "Max"
            "PLUS" -> "Plus"
            else -> "Free"
        }

    val formattedCoinBalance: String
        get() = coinBalance.toString()

    val trialProgress: Float
        get() = if (totalTrialSessions > 0) trialSessionsUsed.toFloat() / totalTrialSessions else 0f
    
    val remainingTrialSessions: Int
        get() = (totalTrialSessions - trialSessionsUsed).coerceAtLeast(0)
}

data class RecommendedSession(
    val id: Long,
    val title: String,
    val matchReason: String,
    val durationMin: Int,
    val iconRes: Int? = null,
    val tag: String? = null
)

data class RecentSession(
    val id: Long,
    val title: String,
    val score: Int,
    val date: String,
    val durationMin: Int
)
