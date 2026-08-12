package com.iti.careerpilot.home.presentation.ready

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
) : ViewModel() {

    private val _state = MutableStateFlow(ReadyToPracticeState())
    val state = _state.asStateFlow()

    private val _events = Channel<ReadyToPracticeEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private val trackId: Long?
        get() = savedStateHandle[KEY_TRACK_ID]

    private fun initialize(trackId: Long, trackName: String) {
        if (!savedStateHandle.contains(KEY_TRACK_ID)) {
            savedStateHandle[KEY_TRACK_ID] = trackId
            savedStateHandle[KEY_TRACK_NAME] = trackName
        }
        _state.update {
            it.copy(trackName = savedStateHandle.get<String>(KEY_TRACK_NAME).orEmpty())
        }
    }

    fun onAction(action: ReadyToPracticeAction) {
        when (action) {
            is ReadyToPracticeAction.Initial -> initialize(action.trackId, action.trackName)
            is ReadyToPracticeAction.MicrophonePermissionChanged -> _state.update {
                it.copy(
                    isMicrophoneGranted = action.isGranted,
                    isPermissionDialogVisible = false,
                )
            }

            ReadyToPracticeAction.MicrophoneRowClicked -> _state.update {
                if (it.isMicrophoneGranted) it else it.copy(isPermissionDialogVisible = true)
            }

            ReadyToPracticeAction.PermissionDialogDismissed -> _state.update {
                it.copy(isPermissionDialogVisible = false)
            }

            ReadyToPracticeAction.BeginInterviewClicked -> beginInterview()

            ReadyToPracticeAction.CancelClicked -> sendEvent(ReadyToPracticeEvent.NavigateBack)
        }
    }

    private fun beginInterview() {
        val id = trackId ?: return
        if (!_state.value.canBegin) return
        sendEvent(ReadyToPracticeEvent.NavigateToPractice(trackId = id))
    }

    private fun sendEvent(event: ReadyToPracticeEvent) {
        viewModelScope.launch { _events.send(event) }
    }

    private companion object {
        const val KEY_TRACK_ID = "ready_track_id"
        const val KEY_TRACK_NAME = "ready_track_name"
    }
}
