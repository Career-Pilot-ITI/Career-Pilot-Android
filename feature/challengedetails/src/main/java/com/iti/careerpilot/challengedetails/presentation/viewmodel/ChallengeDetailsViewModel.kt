package com.iti.careerpilot.challengedetails.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iti.careerpilot.challengedetails.domain.repository.ChallengeDetailsRepository
import com.iti.careerpilot.challengedetails.presentation.action.ChallengeDetailsAction
import com.iti.careerpilot.challengedetails.presentation.event.ChallengeDetailsEvent
import com.iti.careerpilot.challengedetails.presentation.state.ChallengeDetailsState
import com.iti.common.result.onError
import com.iti.common.result.onSuccess
import com.iti.common.snackbar.CareerPilotSnackbarController
import com.iti.common.util.toUIText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChallengeDetailsViewModel @Inject constructor(
    private val repository: ChallengeDetailsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ChallengeDetailsState())
    val state = _state.asStateFlow()

    private val _events = Channel<ChallengeDetailsEvent>()
    val events = _events.receiveAsFlow()

    fun onAction(action: ChallengeDetailsAction) {
        when (action) {
            is ChallengeDetailsAction.Initial -> fetchChallenge(action.challengeId)
            ChallengeDetailsAction.MicrophoneRowClicked -> _state.update { it.copy(isPermissionDialogVisible = true) }
            ChallengeDetailsAction.CameraRowClicked -> _state.update { it.copy(showCameraPermissionDialog = true) }
            ChallengeDetailsAction.PermissionDialogDismissed -> _state.update { it.copy(isPermissionDialogVisible = false) }
            ChallengeDetailsAction.CameraPermissionDialogDismissed -> _state.update { it.copy(showCameraPermissionDialog = false) }
            is ChallengeDetailsAction.MicrophonePermissionChanged -> _state.update { it.copy(isMicrophoneGranted = action.isGranted) }
            is ChallengeDetailsAction.CameraPermissionChanged -> _state.update { it.copy(isCameraGranted = action.isGranted) }
            ChallengeDetailsAction.BeginChallengeClicked -> {
                _state.value.challenge?.let { challenge ->
                    viewModelScope.launch {
                        _events.send(ChallengeDetailsEvent.NavigateToPractice(challenge))
                    }
                }
            }
            ChallengeDetailsAction.OnBackClicked -> {
                viewModelScope.launch {
                    _events.send(ChallengeDetailsEvent.NavigateBack)
                }
            }
        }
    }

    private fun fetchChallenge(challengeId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            repository.getChallenge(challengeId)
                .onSuccess { challenge ->
                    _state.update { it.copy(challenge = challenge, isLoading = false) }
                }
                .onError { error ->
                    _state.update { it.copy(isLoading = false) }
                    CareerPilotSnackbarController.show(error.toUIText())
                }
        }
    }
}
