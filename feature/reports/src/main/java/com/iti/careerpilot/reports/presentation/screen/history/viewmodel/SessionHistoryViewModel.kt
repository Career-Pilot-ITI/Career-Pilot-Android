package com.iti.careerpilot.reports.presentation.screen.history.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iti.careerpilot.reports.domain.usecase.GetSessionHistoryUseCase
import com.iti.careerpilot.reports.presentation.screen.history.contract.SessionHistoryAction
import com.iti.careerpilot.reports.presentation.screen.history.contract.SessionHistoryEvent
import com.iti.careerpilot.reports.presentation.screen.history.contract.SessionHistoryState
import com.iti.careerpilot.reports.presentation.screen.history.uimodels.toUiModel
import com.iti.common.network.NetworkMonitor
import com.iti.common.result.CareerPilotResult
import com.iti.common.util.toUIText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SessionHistoryViewModel @Inject constructor(
    private val getSessionHistory: GetSessionHistoryUseCase,
    networkMonitor: NetworkMonitor,
) : ViewModel() {
    private val mutableState = MutableStateFlow(
        SessionHistoryState(isOnline = networkMonitor.isOnline.value),
    )
    val state = mutableState
        .combine(networkMonitor.isOnline) { currentState, isOnline ->
            if (currentState.isOnline == isOnline) currentState else currentState.copy(isOnline = isOnline)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Companion.WhileSubscribed(5_000L),
            initialValue = mutableState.value,
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
                val exists = mutableState.value.content?.sessions?.any { it.id == action.sessionId } == true
                if (exists) {
                    eventChannel.trySend(SessionHistoryEvent.NavigateToSessionDetails(action.sessionId))
                }
            }
        }
    }

    private fun loadHistory() {
        if (mutableState.value.isLoading) return
        viewModelScope.launch {
            mutableState.update { it.copy(isLoading = true, error = null) }
            when (val result = getSessionHistory()) {
                is CareerPilotResult.Success -> mutableState.update {
                    it.copy(
                        isLoading = false,
                        content = result.data.toUiModel(),
                        error = null,
                    )
                }

                is CareerPilotResult.Error -> mutableState.update {
                    it.copy(
                        isLoading = false,
                        error = result.error.toUIText(),
                    )
                }
            }
        }
    }
}