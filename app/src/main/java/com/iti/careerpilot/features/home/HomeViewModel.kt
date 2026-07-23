package com.iti.careerpilot.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iti.careerpilot.features.paywall.data.remote.UserSyncManager
import com.iti.core.datastore.repo.UserProfileRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userProfileRepo: UserProfileRepo,
    private val userSyncManager: UserSyncManager
) : ViewModel() {

    private var hasLoadedInitialData = false

    private val _state = MutableStateFlow(HomeState())
    val state = _state
        .onStart {
            if (!hasLoadedInitialData) {
                // Observe the DataStore as single source of truth
                userProfileRepo.userProfile.onEach { profile ->
                    _state.value = _state.value.copy(
                        coinBalance = profile.account.coinBalance,
                        subscriptionTier = profile.account.subscriptionTier
                    )
                }.launchIn(viewModelScope)

                // Fetch fresh data from the server so the home screen always
                // reflects the latest balance/tier (e.g. after a payment).
                syncFromServer()

                hasLoadedInitialData = true
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = HomeState()
        )

    private fun syncFromServer() {
        viewModelScope.launch {
            try {
                userSyncManager.syncWalletBalance()
                userSyncManager.syncSubscriptionTier()
            } catch (e: Exception) {
                // Non-fatal: the DataStore will still emit whatever was cached
            }
        }
    }

    fun onAction(action: HomeAction) {
        when (action) {
            else -> TODO("Handle actions")
        }
    }

}