package com.iti.careerpilot.reports.presentation.screen.details.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iti.careerpilot.reports.R
import com.iti.careerpilot.reports.domain.usecase.GetReportDetailsUseCase
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
class ReportDetailsViewModel @Inject constructor(
    private val getReportDetails: GetReportDetailsUseCase,
    networkMonitor: NetworkMonitor,
) : ViewModel() {
    private val mutableState = MutableStateFlow(
        ReportDetailsState(isOnline = networkMonitor.isOnline.value),
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

    private val eventChannel = Channel<ReportDetailsEvent>(Channel.Factory.BUFFERED)
    val events = eventChannel.receiveAsFlow()

    fun onAction(action: ReportDetailsAction) {
        when (action) {
            is ReportDetailsAction.Load -> loadReport(action.sessionId)
            ReportDetailsAction.Retry -> mutableState.value.sessionId?.let { loadReport(it, force = true) }
            ReportDetailsAction.BackClicked -> eventChannel.trySend(ReportDetailsEvent.NavigateBack)
            ReportDetailsAction.QuestionBreakdownClicked -> {
                mutableState.value.content?.sessionId?.let { sessionId ->
                    eventChannel.trySend(
                        ReportDetailsEvent.NavigateToQuestionBreakdown(sessionId),
                    )
                }
            }
        }
    }

    private fun loadReport(
        sessionId: String,
        force: Boolean = false,
    ) {
        if (sessionId.isBlank()) {
            mutableState.update {
                it.copy(
                    sessionId = sessionId,
                    isLoading = false,
                    error = UIText.StringResource(R.string.reports_report_not_found),
                )
            }
            return
        }
        val currentState = mutableState.value
        if (currentState.isLoading || (!force && currentState.content?.sessionId == sessionId)) return
        viewModelScope.launch {
            mutableState.update {
                it.copy(sessionId = sessionId, isLoading = true, error = null)
            }
            when (val result = getReportDetails(sessionId)) {
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
                        error = if (result.error == NetworkError.NOT_FOUND) {
                            UIText.StringResource(R.string.reports_report_not_found)
                        } else {
                            result.error.toUIText()
                        },
                    )
                }
            }
        }
    }
}