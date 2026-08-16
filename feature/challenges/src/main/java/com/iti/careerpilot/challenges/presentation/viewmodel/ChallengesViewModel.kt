package com.iti.careerpilot.challenges.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iti.careerpilot.challenges.domain.repository.ChallengesRepository
import com.iti.careerpilot.challenges.presentation.action.ChallengesAction
import com.iti.careerpilot.challenges.presentation.event.ChallengesEvent
import com.iti.careerpilot.challenges.presentation.state.ChallengesState
import com.iti.careerpilot.core.access.PlanAccessMap
import com.iti.careerpilot.core.access.domain.usecase.CheckFeatureAccessUseCase
import com.iti.common.result.onError
import com.iti.common.result.onSuccess
import com.iti.common.snackbar.CareerPilotSnackbarController
import com.iti.common.util.UIText
import com.iti.common.util.toUIText
import com.iti.core.model.FeatureAccess
import com.iti.core.model.FeatureKey
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChallengesViewModel @Inject constructor(
    private val repository: ChallengesRepository,
    private val checkFeatureAccess: CheckFeatureAccessUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ChallengesState())
    val state = _state.asStateFlow()

    private val _events = Channel<ChallengesEvent>()
    val events = _events.receiveAsFlow()

    private var hasInitialized = false

    private fun initialize() {
        if (hasInitialized) return
        hasInitialized = true
        fetchPublicChallenges()
    }

    private fun fetchPublicChallenges(isRefreshing: Boolean = false) {
        viewModelScope.launch {
            if (isRefreshing) {
                _state.update { it.copy(isRefreshing = true) }
            } else {
                _state.update { it.copy(isLoading = true) }
            }
            repository.getPublicChallenges()
                .onSuccess { challenges ->
                    _state.update { it.copy(
                        publicChallenges = challenges.toImmutableList(),
                        isLoading = false,
                        isRefreshing = false
                    ) }
                }
                .onError { error ->
                    _state.update { it.copy(isLoading = false, isRefreshing = false) }
                    CareerPilotSnackbarController.show(error.toUIText())
                }
        }
    }

    fun onAction(action: ChallengesAction) {
        when (action) {
            ChallengesAction.Initial -> initialize()
            ChallengesAction.Refresh -> fetchPublicChallenges(isRefreshing = true)
            is ChallengesAction.OnSearchQueryChange -> _state.update { it.copy(searchQuery = action.query) }
            ChallengesAction.TogglePrivateCodeDialog -> _state.update { it.copy(isPrivateCodeDialogOpen = !it.isPrivateCodeDialogOpen, privateCode = "") }
            is ChallengesAction.OnPrivateCodeChange -> _state.update { it.copy(privateCode = action.code) }
            ChallengesAction.SubmitPrivateCode -> validateAndNavigateToPrivateChallenge()
            is ChallengesAction.OnChallengeClicked -> {
                viewModelScope.launch {
                    _events.send(ChallengesEvent.NavigateToChallengeDetails(action.challengeId))
                }
            }
            ChallengesAction.CreateChallengeClicked -> {
                viewModelScope.launch {
                    val access = checkFeatureAccess(FeatureKey.CreateChallenge).first()
                    when (access) {
                        is FeatureAccess.Granted -> {
                            _events.send(ChallengesEvent.NavigateToCreateChallenge)
                        }
                        is FeatureAccess.Locked -> {
                            _events.send(
                                ChallengesEvent.ShowFeatureGate(
                                    featureName = FeatureKey.CreateChallenge.displayName(),
                                    requiredPlan = access.requiredPlan,
                                    planFeatures = PlanAccessMap.featuresFor(access.requiredPlan).map { it.displayName() }
                                )
                            )
                        }
                        is FeatureAccess.CoinTopUpRequired -> {
                            _events.send(ChallengesEvent.NavigateToPlansPaywall)
                        }
                        is FeatureAccess.StaleCacheBlocked -> {
                            CareerPilotSnackbarController.show(UIText.DynamicString("Unable to verify access. Please connect to internet."))
                        }
                        FeatureAccess.Unknown -> Unit
                    }
                }
            }
            ChallengesAction.ChallengeDashboardClicked -> {
                viewModelScope.launch {
                    _events.send(ChallengesEvent.NavigateToChallengeDashboard)
                }
            }
        }
    }

    private fun validateAndNavigateToPrivateChallenge() {
        val code = _state.value.privateCode.trim()
        if (code.isBlank()) return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            repository.checkChallengeExists(code)
                .onSuccess { exists ->
                    _state.update { it.copy(isLoading = false) }
                    if (exists) {
                        _state.update { it.copy(isPrivateCodeDialogOpen = false) }
                        _events.send(ChallengesEvent.NavigateToChallengeDetails(code))
                    } else {
                        CareerPilotSnackbarController.show(UIText.DynamicString("Challenge not found"))
                    }
                }
                .onError { error ->
                    _state.update { it.copy(isLoading = false) }
                    CareerPilotSnackbarController.show(error.toUIText())
                }
        }
    }
}
