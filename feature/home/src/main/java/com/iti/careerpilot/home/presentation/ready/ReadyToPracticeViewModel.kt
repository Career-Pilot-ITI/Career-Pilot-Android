package com.iti.careerpilot.home.presentation.ready

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iti.careerpilot.home.domain.usecase.GetUserProfileUseCase
import com.iti.core.datastore.models.isPaidSubscriber
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReadyToPracticeViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val getUserProfileUseCase: GetUserProfileUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(ReadyToPracticeState())
    val state = _state.asStateFlow()

    private val _events = Channel<ReadyToPracticeEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private val trackId: Long?
        get() = savedStateHandle[KEY_TRACK_ID]
    private val workspaceId: Long?
        get() = savedStateHandle[KEY_WORKSPACE_ID]

    private fun initialize(trackId: Long, trackName: String, workspaceId: Long?) {
        if (!savedStateHandle.contains(KEY_TRACK_ID)) {
            savedStateHandle[KEY_TRACK_ID] = trackId
            savedStateHandle[KEY_TRACK_NAME] = trackName
            workspaceId?.let { savedStateHandle[KEY_WORKSPACE_ID] = it }
        }
        _state.update {
            it.copy(trackName = savedStateHandle.get<String>(KEY_TRACK_NAME).orEmpty())
        }
        observeUserProfile()
    }

    private fun observeUserProfile() {
        viewModelScope.launch {
            getUserProfileUseCase().collect { profile ->
                val isPaid = profile.isPaidSubscriber()
                _state.update {
                    it.copy(
                        isPaidPlan = isPaid,
                        isVideoMode = if (isPaid) it.isVideoMode else false
                    )
                }
            }
        }
    }

    fun initialise(trackId: Long, trackName: String) {
        if (savedStateHandle.contains(KEY_TRACK_ID)) return
        savedStateHandle[KEY_TRACK_ID] = trackId
        savedStateHandle[KEY_TRACK_NAME] = trackName
        _state.update { it.copy(trackName = trackName) }
    }

    fun onAction(action: ReadyToPracticeAction) {
        when (action) {
            is ReadyToPracticeAction.Initial -> initialize(
                action.trackId,
                action.trackName,
                action.workspaceId,
            )
            is ReadyToPracticeAction.MicrophonePermissionChanged -> _state.update {
                it.copy(
                    isMicrophoneGranted = action.isGranted,
                    isPermissionDialogVisible = false,
                )
            }

            is ReadyToPracticeAction.CameraPermissionChanged -> _state.update {
                it.copy(
                    isCameraGranted = action.isGranted,
                    showCameraPermissionDialog = false,
                )
            }

            ReadyToPracticeAction.SelectAudioMode -> _state.update {
                it.copy(
                    isVideoMode = false,
                    enablePostureTracking = false,
                    enableHandTracking = false
                )
            }

            is ReadyToPracticeAction.TogglePostureTracking -> _state.update {
                it.copy(enablePostureTracking = action.enabled)
            }

            is ReadyToPracticeAction.ToggleHandTracking -> _state.update {
                it.copy(enableHandTracking = action.enabled)
            }

            ReadyToPracticeAction.SelectVideoMode -> {
                if (_state.value.isPaidPlan) {
                    _state.update { 
                        it.copy(
                            isVideoMode = true,
                            showCameraPermissionDialog = !it.isCameraGranted
                        ) 
                    }
                } else {
                    sendEvent(ReadyToPracticeEvent.NavigateToPaywall)
                }
            }

            ReadyToPracticeAction.MicrophoneRowClicked -> _state.update {
                if (it.isMicrophoneGranted) it else it.copy(isPermissionDialogVisible = true)
            }

            ReadyToPracticeAction.PermissionDialogDismissed -> _state.update {
                it.copy(isPermissionDialogVisible = false)
            }

            ReadyToPracticeAction.CameraRowClicked -> _state.update {
                if (it.isCameraGranted) it else it.copy(showCameraPermissionDialog = true)
            }

            ReadyToPracticeAction.CameraPermissionDialogDismissed -> _state.update {
                it.copy(showCameraPermissionDialog = false)
            }

            ReadyToPracticeAction.BeginInterviewClicked -> beginInterview()

            ReadyToPracticeAction.CancelClicked -> sendEvent(ReadyToPracticeEvent.NavigateBack)
        }
    }

    private fun beginInterview() {
        val id = trackId ?: return
        if (!_state.value.canBegin) return
        val s = _state.value
        sendEvent(
            ReadyToPracticeEvent.NavigateToPractice(
                trackId = id,
                workspaceId = workspaceId,
                isVideo = s.isVideoMode,
                enablePosture = s.enablePostureTracking,
                enableHands = s.enableHandTracking,
            )
        )
    }

    private fun sendEvent(event: ReadyToPracticeEvent) {
        viewModelScope.launch { _events.send(event) }
    }

    private companion object {
        const val KEY_TRACK_ID = "ready_track_id"
        const val KEY_TRACK_NAME = "ready_track_name"
        const val KEY_WORKSPACE_ID = "ready_workspace_id"
    }
}
