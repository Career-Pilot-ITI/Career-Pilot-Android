package com.iti.careerpilot.challengedashboard.presentation.state

import com.iti.careerpilot.challengefirestore.Challenge
import com.iti.careerpilot.challengefirestore.ChallengeSession
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class ChallengeDashboardState(
    val isLoading: Boolean = false,
    val createdChallenges: ImmutableList<Challenge> = persistentListOf(),
    val takenChallenges: ImmutableList<ChallengeSession> = persistentListOf(),
    val selectedTab: Int = 0, // 0: My Challenges, 1: Taken Challenges
    val participantSessions: ImmutableList<ChallengeSession>? = null, // Sessions for a specific created challenge
    val selectedChallengeId: String? = null
)
