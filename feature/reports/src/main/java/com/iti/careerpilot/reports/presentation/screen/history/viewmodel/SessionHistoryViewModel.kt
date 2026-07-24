package com.iti.careerpilot.reports.presentation.screen.history.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iti.careerpilot.reports.domain.usecase.GetSessionHistoryUseCase
import com.iti.careerpilot.reports.presentation.screen.components.ReportsContentPhase
import com.iti.careerpilot.reports.presentation.screen.history.contract.SessionHistoryAction
import com.iti.careerpilot.reports.presentation.screen.history.contract.SessionHistoryEvent
import com.iti.careerpilot.reports.presentation.screen.history.contract.SessionHistoryState
import com.iti.careerpilot.reports.presentation.screen.history.uimodels.toUiModel
import com.iti.common.network.NetworkMonitor
import com.iti.common.result.CareerPilotResult
import com.iti.common.util.toUIText
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class SessionHistoryViewModel @Inject constructor(
    private val getSessionHistory: GetSessionHistoryUseCase,
    networkMonitor: NetworkMonitor,
) : ViewModel() {
    private val _state = MutableStateFlow(
        SessionHistoryState(isOnline = networkMonitor.isOnline.value),
    )
    val state = _state
        .combine(networkMonitor.isOnline) { currentState, isOnline ->
            currentState.copy(isOnline = isOnline)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Companion.WhileSubscribed(5_000L),
            initialValue = _state.value,
        )

    private val eventChannel = Channel<SessionHistoryEvent>(Channel.Factory.BUFFERED)
    val events = eventChannel.receiveAsFlow()

    init {
        loadHistory()
    }

    fun onAction(action: SessionHistoryAction) {
        when (action) {
            SessionHistoryAction.Load,
            SessionHistoryAction.Retry,
            -> loadHistory()

            is SessionHistoryAction.SessionClicked -> {
                val exists = _state.value.content?.sessions?.any { it.id == action.sessionId } == true
                if (exists) {
                    eventChannel.trySend(SessionHistoryEvent.NavigateToSessionDetails(action.sessionId))
                }
            }
        }
    }

    private fun loadHistory() {
        if (_state.value.isLoading) return
        viewModelScope.launch {
            _state.update { currentState ->
                currentState.copy(
                    isLoading = true,
                    error = null,
                    phase = if (currentState.content == null) {
                        ReportsContentPhase.LOADING
                    } else {
                        currentState.phase
                    },
                )
            }
            when (val result = getSessionHistory()) {
                is CareerPilotResult.Success -> {
                    val content = result.data.toUiModel()
                    _state.update {
                        it.copy(
                            isLoading = false,
                            content = content,
                            error = null,
                            phase = if (content.sessions.isEmpty()) {
                                ReportsContentPhase.EMPTY
                            } else {
                                ReportsContentPhase.CONTENT
                            },
                        )
                    }
                }

                is CareerPilotResult.Error -> _state.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        error = result.error.toUIText(),
                        phase = if (currentState.content == null) {
                            ReportsContentPhase.ERROR
                        } else {
                            currentState.phase
                        },
                    )
                }
            }
        }
    }
}
