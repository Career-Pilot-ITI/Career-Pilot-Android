package com.iti.careerpilot.reports.presentation.screen.breakdown.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iti.careerpilot.reports.R
import com.iti.careerpilot.reports.domain.usecase.GetQuestionBreakdownUseCase
import com.iti.careerpilot.reports.presentation.screen.breakdown.contract.QuestionBreakdownAction
import com.iti.careerpilot.reports.presentation.screen.breakdown.contract.QuestionBreakdownEvent
import com.iti.careerpilot.reports.presentation.screen.breakdown.contract.QuestionBreakdownState
import com.iti.careerpilot.reports.presentation.screen.breakdown.uimodels.toUiModel
import com.iti.careerpilot.reports.presentation.screen.components.ReportsContentPhase
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
class QuestionBreakdownViewModel @Inject constructor(
    private val getQuestionBreakdown: GetQuestionBreakdownUseCase,
    private val savedStateHandle: SavedStateHandle,
    networkMonitor: NetworkMonitor,
) : ViewModel() {
    private val _state = MutableStateFlow(
        QuestionBreakdownState(
            selectedQuestionId = savedStateHandle[SELECTED_QUESTION_ID],
            isOnline = networkMonitor.isOnline.value,
        ),
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

    private val eventChannel = Channel<QuestionBreakdownEvent>(Channel.BUFFERED)
    val events = eventChannel.receiveAsFlow()

    fun onAction(action: QuestionBreakdownAction) {
        when (action) {
            is QuestionBreakdownAction.Load -> loadBreakdown(action.sessionId)
            QuestionBreakdownAction.Retry -> _state.value.sessionId?.let {
                loadBreakdown(it, force = true)
            }

            QuestionBreakdownAction.BackClicked -> viewModelScope.launch {
                eventChannel.send(QuestionBreakdownEvent.NavigateBack)
            }

            is QuestionBreakdownAction.QuestionSelected -> selectQuestion(action.questionId)
        }
    }

    private fun loadBreakdown(
        sessionId: Long,
        force: Boolean = false,
    ) {
        if (sessionId <= 0L) {
            _state.update {
                it.copy(
                    sessionId = sessionId,
                    isLoading = false,
                    content = null,
                    selectedQuestionId = null,
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
                    selectedQuestionId = if (isNewSession) null else state.selectedQuestionId,
                    error = null,
                    phase = if (isNewSession) {
                        ReportsContentPhase.LOADING
                    } else {
                        state.phase
                    },
                )
            }
            when (val result = getQuestionBreakdown(sessionId)) {
                is CareerPilotResult.Success -> {
                    val content = result.data.toUiModel()
                    val restoredId = savedStateHandle.get<String>(SELECTED_QUESTION_ID)
                    val selectedId = restoredId
                        ?.takeIf { id -> content.questions.any { it.id == id } }
                        ?: content.questions.firstOrNull()?.id
                    savedStateHandle[SELECTED_QUESTION_ID] = selectedId
                    _state.update {
                        it.copy(
                            isLoading = false,
                            content = content,
                            selectedQuestionId = selectedId,
                            error = null,
                            phase = if (selectedId == null) {
                                ReportsContentPhase.EMPTY
                            } else {
                                ReportsContentPhase.CONTENT
                            },
                        )
                    }
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

    private fun selectQuestion(questionId: String) {
        val isValid = _state.value.content?.questions?.any { it.id == questionId } == true
        if (!isValid || _state.value.selectedQuestionId == questionId) return
        savedStateHandle[SELECTED_QUESTION_ID] = questionId
        _state.update {
            it.copy(
                selectedQuestionId = questionId,
                phase = ReportsContentPhase.CONTENT,
            )
        }
    }

    private companion object {
        const val SELECTED_QUESTION_ID = "reports_selected_question_id"
    }
}
