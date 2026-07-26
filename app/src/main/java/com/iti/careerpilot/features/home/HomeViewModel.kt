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
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userProfileRepo: UserProfileRepo,
    private val userSyncManager: UserSyncManager
) : ViewModel() {

    val state: StateFlow<HomeState> = userProfileRepo.userProfile
        .map { profile ->
            HomeState(
                coinBalance = profile.account.coinBalance,
                subscriptionTier = profile.account.subscriptionTier
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