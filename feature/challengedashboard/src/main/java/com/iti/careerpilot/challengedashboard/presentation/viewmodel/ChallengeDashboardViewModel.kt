package com.iti.careerpilot.challengedashboard.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iti.careerpilot.challengedashboard.domain.repository.ChallengeDashboardRepository
import com.iti.careerpilot.challengedashboard.presentation.action.ChallengeDashboardAction
import com.iti.careerpilot.challengedashboard.presentation.event.ChallengeDashboardEvent
import com.iti.careerpilot.challengedashboard.presentation.state.ChallengeDashboardState
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

    private fun loadData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
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

            _state.update { it.copy(isLoading = false) }
        }
    }

    fun onAction(action: ChallengeDashboardAction) {
        when (action) {
            ChallengeDashboardAction.Initial -> initialize()
            ChallengeDashboardAction.Refresh -> loadData()
            is ChallengeDashboardAction.OnTabSelected -> _state.update { it.copy(selectedTab = action.index) }
            is ChallengeDashboardAction.OnDeleteChallenge -> deleteChallenge(
                action.challengeId,
                action.visibility
            )

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
            is ChallengeDashboardAction.OnTakenChallengeClicked -> {
                viewModelScope.launch {
                    _events.send(
                        ChallengeDashboardEvent.NavigateToSessionDetails(
                            action.sessionId
                        )
                    )
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
        visibility: com.iti.core.model.ChallengeVisibility
    ) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
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
