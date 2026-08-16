package com.iti.careerpilot.challenges.presentation.state

import com.iti.careerpilot.challengefirestore.Challenge
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class ChallengesState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val publicChallenges: ImmutableList<Challenge> = persistentListOf(),
    val searchQuery: String = "",
    val isPrivateCodeDialogOpen: Boolean = false,
    val privateCode: String = "",
) {
    val filteredChallenges: List<Challenge>
        get() = if (searchQuery.isBlank()) {
            publicChallenges
        } else {
            publicChallenges.filter { 
                it.trackName.contains(searchQuery, ignoreCase = true) || 
                it.creatorName.contains(searchQuery, ignoreCase = true) 
            }
        }
}
