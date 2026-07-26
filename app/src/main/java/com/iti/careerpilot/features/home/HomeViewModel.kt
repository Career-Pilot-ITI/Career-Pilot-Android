package com.iti.careerpilot.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iti.careerpilot.features.paywall.data.remote.UserSyncManager
import com.iti.core.datastore.repo.UserProfileRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.collections.immutable.persistentListOf
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userProfileRepo: UserProfileRepo,
    private val userSyncManager: UserSyncManager
) : ViewModel() {

    val state: StateFlow<HomeState> = userProfileRepo.userProfile
        .map { profile ->
            HomeState(
                userName = profile.personal.displayName,
                coinBalance = profile.account.coinBalance,
                subscriptionTier = profile.account.subscriptionTier,
                trialSessionsUsed = 1, // Dummy
                totalTrialSessions = 3, // Dummy
                overallScore = 78, // Dummy
                scoreTrend = "+6 from last week", // Dummy
                scoreFeedback = "Good Progress", // Dummy
                recommendedSessions = persistentListOf(
                    RecommendedSession(
                        id = 1,
                        title = "System Design Basics",
                        matchReason = "System Design",
                        durationMin = 20
                    ),
                    RecommendedSession(
                        id = 2,
                        title = "React Deep Dive",
                        matchReason = "React",
                        durationMin = 15
                    )
                ),
                recentSessions = persistentListOf(
                    RecentSession(
                        id = 101,
                        title = "Software Eng.",
                        score = 82,
                        date = "Today, 2:14 PM",
                        durationMin = 18
                    ),
                    RecentSession(
                        id = 102,
                        title = "Software Eng.",
                        score = 74,
                        date = "Yesterday, 10:30 AM",
                        durationMin = 22
                    ),
                    RecentSession(
                        id = 103,
                        title = "System Design",
                        score = 68,
                        date = "Mon, 9:00 AM",
                        durationMin = 15
                    )
                )
            )
        }
        .onStart {
            syncFromServer()
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = HomeState()
        )

    fun syncFromServer() {
        viewModelScope.launch {
            try {
                userSyncManager.syncWalletBalance()
                userSyncManager.syncSubscriptionTier()
            } catch (e: Exception) {
                // Non-fatal: DataStore will still emit cached data
            }
        }
    }

    fun onAction(action: HomeAction) {
        // Handle actions
    }
}