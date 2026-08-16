package com.iti.careerpilot.challengedashboard.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iti.careerpilot.challengedashboard.domain.repository.ChallengeDashboardRepository
import com.iti.careerpilot.challengedashboard.presentation.action.ChallengeDashboardAction
import com.iti.careerpilot.challengedashboard.presentation.event.ChallengeDashboardEvent
import com.iti.careerpilot.challengedashboard.presentation.state.ChallengeDashboardState
import com.iti.careerpilot.challengefirestore.ChallengeVisibility
import com.iti.common.result.onError
import com.iti.common.result.onSuccess
import com.iti.common.snackbar.CareerPilotSnackbarController
import com.iti.common.util.toUIText
import com.iti.core.datastore.repo.UserProfileRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChallengeDashboardViewModel @Inject constructor(
    private val repository: ChallengeDashboardRepository,
    private val userProfileRepo: UserProfileRepo
) : ViewModel() {

    private val _state = MutableStateFlow(ChallengeDashboardState())
    val state = _state.asStateFlow()

    private val _events = Channel<ChallengeDashboardEvent>()
    val events = _events.receiveAsFlow()

    private var hasInitialized = false

    private fun initialize() {
        if (hasInitialized) return
        hasInitialized = true
        loadData()
    }

    private fun loadData(isRefreshing: Boolean = false) {
        viewModelScope.launch {
            if (isRefreshing) {
                _state.update { it.copy(isRefreshing = true) }
            } else {
                _state.update { it.copy(isLoading = true) }
            }
            val profile = userProfileRepo.readUserProfile()
            val userId = profile.id

            val createdResult = async { repository.getCreatedChallenges(userId) }
            val takenResult = async { repository.getTakenChallenges(userId) }

            createdResult.await().onSuccess { challenges ->
                _state.update { it.copy(createdChallenges = challenges.toImmutableList()) }
            }

            takenResult.await().onSuccess { sessions ->
                _state.update { it.copy(takenChallenges = sessions.toImmutableList()) }
            }

            _state.update { it.copy(isLoading = false, isRefreshing = false) }
        }
    }

    fun onAction(action: ChallengeDashboardAction) {
        when (action) {
            ChallengeDashboardAction.Initial -> initialize()
            ChallengeDashboardAction.Refresh -> loadData(isRefreshing = true)
            is ChallengeDashboardAction.OnTabSelected -> _state.update { it.copy(selectedTab = action.tab) }
            is ChallengeDashboardAction.OnDeleteChallenge -> _state.update { it.copy(challengeToDelete = action.challenge) }
            ChallengeDashboardAction.OnConfirmDelete -> {
                _state.value.challengeToDelete?.let { challenge ->
                    deleteChallenge(challenge.id, challenge.visibility)
                }
            }

            ChallengeDashboardAction.OnDismissDeleteConfirmation -> _state.update { it.copy(challengeToDelete = null) }

            is ChallengeDashboardAction.OnEditChallenge -> {
                viewModelScope.launch {
                    _events.send(
                        ChallengeDashboardEvent.NavigateToEditChallenge(
                            action.challengeId
                        )
                    )
                }
            }

            is ChallengeDashboardAction.OnViewParticipantReports -> loadParticipantSessions(action.challengeId)
            is ChallengeDashboardAction.OnViewSessionResult -> {
                viewModelScope.launch {
                    _events.send(ChallengeDashboardEvent.NavigateToSessionResult(action.sessionId))
                }
            }
            is ChallengeDashboardAction.OnTakenChallengeClicked -> {
                val session = _state.value.takenChallenges.find { it.sessionId == action.sessionId }
                    ?: _state.value.participantSessions?.find { it.sessionId == action.sessionId }

                viewModelScope.launch {
                    if (session?.status == "COMPLETED") {
                        _events.send(
                            ChallengeDashboardEvent.NavigateToSessionResult(
                                action.sessionId
                            )
                        )
                    } else {
                        _events.send(
                            ChallengeDashboardEvent.ContinueSession(
                                action.sessionId
                            )
                        )
                    }
                }
            }

            ChallengeDashboardAction.OnDismissParticipantReports -> _state.update {
                it.copy(
                    participantSessions = null,
                    selectedChallengeId = null
                )
            }

            ChallengeDashboardAction.OnBackClicked -> {
                viewModelScope.launch { _events.send(ChallengeDashboardEvent.NavigateBack) }
            }
        }
    }

    private fun deleteChallenge(
        challengeId: String,
        visibility: ChallengeVisibility
    ) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, challengeToDelete = null) }
            repository.deleteChallenge(challengeId, visibility)
                .onSuccess {
                    loadData()
                }
                .onError { error ->
                    _state.update { it.copy(isLoading = false) }
                    CareerPilotSnackbarController.show(error.toUIText())
                }
        }
    }

    private fun loadParticipantSessions(challengeId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, selectedChallengeId = challengeId) }
            repository.getChallengeSessions(challengeId)
                .onSuccess { sessions ->
                    _state.update {
                        it.copy(
                            participantSessions = sessions.toImmutableList(),
                            isLoading = false
                        )
                    }
                }
                .onError { error ->
                    _state.update { it.copy(isLoading = false) }
                    CareerPilotSnackbarController.show(error.toUIText())
                }
        }
    }
}
