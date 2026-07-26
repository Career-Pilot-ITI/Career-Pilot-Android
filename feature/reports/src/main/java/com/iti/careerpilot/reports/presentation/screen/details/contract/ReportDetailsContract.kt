package com.iti.careerpilot.reports.presentation.screen.details.contract

import androidx.compose.runtime.Immutable
import com.iti.careerpilot.reports.presentation.screen.components.ReportsContentPhase
import com.iti.careerpilot.reports.presentation.screen.details.uimodels.ReportDetailsUiModel
import com.iti.common.util.UIText

@Immutable
data class ReportDetailsState(
    val sessionId: Long? = null,
    val isLoading: Boolean = false,
    val content: ReportDetailsUiModel? = null,
    val isOnline: Boolean = true,
    val error: UIText? = null,
    val phase: ReportsContentPhase = ReportsContentPhase.LOADING,
)

sealed interface ReportDetailsAction {
    data class Load(val sessionId: Long) : ReportDetailsAction
    data object Retry : ReportDetailsAction
    data object BackClicked : ReportDetailsAction
    data object QuestionBreakdownClicked : ReportDetailsAction
}

sealed interface ReportDetailsEvent {
    data object NavigateBack : ReportDetailsEvent
    data class NavigateToQuestionBreakdown(val sessionId: Long) : ReportDetailsEvent
}
