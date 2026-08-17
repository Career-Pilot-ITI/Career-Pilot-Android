package com.iti.careerpilot.challengedashboard.presentation.state

import com.iti.careerpilot.challengedashboard.R
import com.iti.careerpilot.challengefirestore.Challenge
import com.iti.careerpilot.challengefirestore.ChallengeSession
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class ChallengeDashboardState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val createdChallenges: ImmutableList<Challenge> = persistentListOf(),
    val takenChallenges: ImmutableList<ChallengeSession> = persistentListOf(),
    val selectedTab: DashboardTab = DashboardTab.MY_CHALLENGES,
    val participantSessions: ImmutableList<ChallengeSession>? = null, // Sessions for a specific created challenge
    val selectedChallengeId: String? = null,
    val challengeToDelete: Challenge? = null,
    val challengeToShare: Challenge? = null,
    val isShareDialogVisible: Boolean = false,
) {
    val challenge: Challenge? get() = challengeToShare
}

enum class DashboardTab(val titleRes: Int) {
    MY_CHALLENGES(R.string.dashboard_tab_my_challenges),
    HISTORY(R.string.dashboard_tab_history)
}

