package com.iti.careerpilot.reports.presentation.screen.details.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iti.careerpilot.reports.R
import com.iti.careerpilot.reports.domain.usecase.GetReportDetailsUseCase
import com.iti.careerpilot.reports.presentation.screen.components.ReportsContentPhase
import com.iti.careerpilot.reports.presentation.screen.details.contract.ReportDetailsAction
import com.iti.careerpilot.reports.presentation.screen.details.contract.ReportDetailsEvent
import com.iti.careerpilot.reports.presentation.screen.details.contract.ReportDetailsState
import com.iti.careerpilot.reports.presentation.screen.details.uimodels.toUiModel
import com.iti.common.error.NetworkError
import com.iti.common.network.NetworkMonitor
import com.iti.common.result.CareerPilotResult
import com.iti.common.util.UIText
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
class ReportDetailsViewModel @Inject constructor(
    private val getReportDetails: GetReportDetailsUseCase,
    networkMonitor: NetworkMonitor,
) : ViewModel() {
    private val _state = MutableStateFlow(
        ReportDetailsState(isOnline = networkMonitor.isOnline.value),
    )
    val state = _state
        .combine(networkMonitor.isOnline) { currentState, isOnline ->
            currentState.copy(isOnline = isOnline)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = _state.value,
        )

    private val eventChannel = Channel<ReportDetailsEvent>(Channel.BUFFERED)
    val events = eventChannel.receiveAsFlow()

    fun onAction(action: ReportDetailsAction) {
        when (action) {
            is ReportDetailsAction.Load -> loadReport(action.sessionId)
            ReportDetailsAction.Retry -> _state.value.sessionId?.let { loadReport(it, force = true) }
            ReportDetailsAction.BackClicked -> viewModelScope.launch {
                eventChannel.send(ReportDetailsEvent.NavigateBack)
            }
            ReportDetailsAction.QuestionBreakdownClicked -> viewModelScope.launch {
                _state.value.content?.sessionId?.let { sessionId ->
                    eventChannel.send(
                        ReportDetailsEvent.NavigateToQuestionBreakdown(sessionId),
                    )
                }
            }
        }
    }

    private fun loadReport(
        sessionId: Long,
        force: Boolean = false,
    ) {
        if (sessionId <= 0L) {
            _state.update {
                it.copy(
                    sessionId = sessionId,
                    isLoading = false,
                    content = null,
                    error = UIText.StringResource(R.string.reports_report_not_found),
                    phase = ReportsContentPhase.ERROR,
                )
            }
            return
        }
        val currentState = _state.value
        if (currentState.isLoading || (!force && currentState.content?.sessionId == sessionId)) return
        viewModelScope.launch {
            _state.update { state ->
                val isNewSession = state.content?.sessionId != sessionId
                state.copy(
                    sessionId = sessionId,
                    isLoading = true,
                    content = if (isNewSession) null else state.content,
                    error = null,
                    phase = if (isNewSession) {
                        ReportsContentPhase.LOADING
                    } else {
                        state.phase
                    },
                )
            }
            when (val result = getReportDetails(sessionId)) {
                is CareerPilotResult.Success -> _state.update {
                    it.copy(
                        isLoading = false,
                        content = result.data.toUiModel(),
                        error = null,
                        phase = ReportsContentPhase.CONTENT,
                    )
                }

                is CareerPilotResult.Error -> _state.update { state ->
                    state.copy(
                        isLoading = false,
                        error = if (result.error == NetworkError.NOT_FOUND) {
                            UIText.StringResource(R.string.reports_report_not_found)
                        } else {
                            result.error.toUIText()
                        },
                        phase = if (state.content == null) {
                            ReportsContentPhase.ERROR
                        } else {
                            state.phase
                        },
                    )
                }
            }
        }
    }
}
