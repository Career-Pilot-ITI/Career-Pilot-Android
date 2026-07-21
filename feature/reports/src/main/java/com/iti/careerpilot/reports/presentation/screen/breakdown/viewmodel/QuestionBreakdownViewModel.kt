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
    private val mutableState = MutableStateFlow(
        QuestionBreakdownState(
            selectedQuestionId = savedStateHandle[SELECTED_QUESTION_ID],
            isOnline = networkMonitor.isOnline.value,
        ),
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

    private val eventChannel = Channel<QuestionBreakdownEvent>(Channel.Factory.BUFFERED)
    val events = eventChannel.receiveAsFlow()

    fun onAction(action: QuestionBreakdownAction) {
        when (action) {
            is QuestionBreakdownAction.Load -> loadBreakdown(action.sessionId)
            QuestionBreakdownAction.Retry -> mutableState.value.sessionId?.let {
                loadBreakdown(it, force = true)
            }
            QuestionBreakdownAction.BackClicked -> eventChannel.trySend(QuestionBreakdownEvent.NavigateBack)
            is QuestionBreakdownAction.QuestionSelected -> selectQuestion(action.questionId)
        }
    }

    private fun loadBreakdown(
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
            when (val result = getQuestionBreakdown(sessionId)) {
                is CareerPilotResult.Success -> {
                    val content = result.data.toUiModel()
                    val restoredId = mutableState.value.selectedQuestionId
                    val selectedId = restoredId
                        ?.takeIf { id -> content.questions.any { it.id == id } }
                        ?: content.questions.firstOrNull()?.id
                    savedStateHandle[SELECTED_QUESTION_ID] = selectedId
                    mutableState.update {
                        it.copy(
                            isLoading = false,
                            content = content,
                            selectedQuestionId = selectedId,
                            error = null,
                        )
                    }
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

    private fun selectQuestion(questionId: String) {
        val isValid = mutableState.value.content?.questions?.any { it.id == questionId } == true
        if (!isValid || mutableState.value.selectedQuestionId == questionId) return
        savedStateHandle[SELECTED_QUESTION_ID] = questionId
        mutableState.update { it.copy(selectedQuestionId = questionId) }
    }

    private companion object {
        const val SELECTED_QUESTION_ID = "reports_selected_question_id"
    }
}