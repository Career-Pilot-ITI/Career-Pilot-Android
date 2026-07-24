package com.iti.careerpilot.reports.presentation.screen.breakdown.contract

import androidx.compose.runtime.Immutable
import com.iti.careerpilot.reports.presentation.screen.breakdown.uimodels.QuestionBreakdownUiModel
import com.iti.careerpilot.reports.presentation.screen.breakdown.uimodels.QuestionUiModel
import com.iti.careerpilot.reports.presentation.screen.components.ReportsContentPhase
import com.iti.common.util.UIText

@Immutable
data class QuestionBreakdownState(
    val sessionId: Long? = null,
    val isLoading: Boolean = false,
    val content: QuestionBreakdownUiModel? = null,
    val selectedQuestionId: String? = null,
    val isOnline: Boolean = true,
    val error: UIText? = null,
    val phase: ReportsContentPhase = ReportsContentPhase.LOADING,
) {
    val selectedQuestion: QuestionUiModel?
        get() = content?.questions?.firstOrNull { it.id == selectedQuestionId }
}

sealed interface QuestionBreakdownAction {
    data class Load(val sessionId: Long) : QuestionBreakdownAction
    data object Retry : QuestionBreakdownAction
    data object BackClicked : QuestionBreakdownAction
    data class QuestionSelected(val questionId: String) : QuestionBreakdownAction
}

sealed interface QuestionBreakdownEvent {
    data object NavigateBack : QuestionBreakdownEvent
}
