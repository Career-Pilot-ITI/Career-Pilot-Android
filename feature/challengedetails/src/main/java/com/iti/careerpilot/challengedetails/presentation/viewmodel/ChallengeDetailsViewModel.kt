package com.iti.careerpilot.challengedetails.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iti.careerpilot.challengedetails.domain.repository.ChallengeDetailsRepository
import com.iti.careerpilot.challengedetails.presentation.action.ChallengeDetailsAction
import com.iti.careerpilot.challengedetails.presentation.event.ChallengeDetailsEvent
import com.iti.careerpilot.challengedetails.presentation.state.ChallengeDetailsState
import com.iti.careerpilot.challengefirestore.ChallengeType
import com.iti.common.result.onError
import com.iti.common.result.onSuccess
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
            ChallengeDetailsAction.OnBackClicked -> {
                viewModelScope.launch { _events.send(ChallengeDetailsEvent.NavigateBack) }
            }
            ChallengeDetailsAction.BeginChallengeClicked -> handleBegin()
            is ChallengeDetailsAction.MicrophonePermissionChanged -> {
                _state.update { it.copy(isMicrophoneGranted = action.isGranted) }
            }
            is ChallengeDetailsAction.CameraPermissionChanged -> {
                _state.update { it.copy(isCameraGranted = action.isGranted) }
            }
            ChallengeDetailsAction.PermissionDialogDismissed -> _state.update { it.copy(showMicPermissionDialog = false) }
            ChallengeDetailsAction.CameraPermissionDialogDismissed -> _state.update { it.copy(showCameraPermissionDialog = false) }
            ChallengeDetailsAction.MicrophoneRowClicked -> _state.update { it.copy(showMicPermissionDialog = true, showCameraPermissionDialog = false) }
            ChallengeDetailsAction.CameraRowClicked -> _state.update { it.copy(showMicPermissionDialog = true, showCameraPermissionDialog = true) }
            ChallengeDetailsAction.ShareClicked -> _state.update { it.copy(isShareDialogVisible = true) }
            ChallengeDetailsAction.DismissShareDialog -> _state.update { it.copy(isShareDialogVisible = false) }
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
                    _state.update { it.copy(isLoading = false, error = error.toString()) }
                }
        }
    }

    private fun handleBegin() {
        val challenge = _state.value.challenge ?: return
        
        if (!_state.value.isMicrophoneGranted) {
            _state.update { it.copy(showMicPermissionDialog = true) }
            return
        }
        
        if (challenge.type == ChallengeType.VIDEO_AND_AUDIO && !_state.value.isCameraGranted) {
            _state.update { it.copy(showCameraPermissionDialog = true) }
            return
        }

        viewModelScope.launch {
            _events.send(ChallengeDetailsEvent.NavigateToPractice(challenge))
        }
    }
}
