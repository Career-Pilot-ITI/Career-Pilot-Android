package com.iti.careerpilot.home.presentation.ready

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iti.careerpilot.home.domain.usecase.StartInterviewSessionUseCase
import com.iti.common.result.CareerPilotResult
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
class ReadyToPracticeViewModel @Inject constructor(
    private val startInterviewSession: StartInterviewSessionUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(ReadyToPracticeState())
    val state = _state.asStateFlow()

    private val _events = Channel<ReadyToPracticeEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private var trackId: Long? = null

    fun initialise(trackId: Long, trackName: String) {
        if (this.trackId != null) return
        this.trackId = trackId
        _state.update { it.copy(trackName = trackName) }
    }

    fun onAction(action: ReadyToPracticeAction) {
        when (action) {
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

        viewModelScope.launch {
            _state.update { it.copy(isStarting = true, error = null) }

            when (val result = startInterviewSession(id)) {
                is CareerPilotResult.Error -> {
                    _state.update {
                        it.copy(isStarting = false, error = result.error.toUIText())
                    }
                    CareerPilotSnackbarController.show(result.error.toUIText())
                }

                is CareerPilotResult.Success -> {
                    _state.update { it.copy(isStarting = false) }
                    sendEvent(ReadyToPracticeEvent.NavigateToInterview(result.data.sessionId))
                }
            }
        }
    }

    private fun sendEvent(event: ReadyToPracticeEvent) {
        viewModelScope.launch { _events.send(event) }
    }
}
