package com.iti.careerpilot.challenges.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iti.careerpilot.challenges.domain.repository.ChallengesRepository
import com.iti.careerpilot.challenges.presentation.action.ChallengesAction
import com.iti.careerpilot.challenges.presentation.event.ChallengesEvent
import com.iti.careerpilot.challenges.presentation.state.ChallengesState
import com.iti.common.result.onError
import com.iti.common.result.onSuccess
import com.iti.common.snackbar.CareerPilotSnackbarController
import com.iti.common.util.UIText
import com.iti.common.util.toUIText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChallengesViewModel @Inject constructor(
    private val repository: ChallengesRepository
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

    private fun fetchPublicChallenges() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            repository.getPublicChallenges()
                .onSuccess { challenges ->
                    _state.update { it.copy(publicChallenges = challenges.toImmutableList(), isLoading = false) }
                }
                .onError { error ->
                    _state.update { it.copy(isLoading = false) }
                    CareerPilotSnackbarController.show(error.toUIText())
                }
        }
    }

    fun onAction(action: ChallengesAction) {
        when (action) {
            ChallengesAction.Initial -> initialize()
            ChallengesAction.Refresh -> fetchPublicChallenges()
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
                    _events.send(ChallengesEvent.NavigateToCreateChallenge)
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
