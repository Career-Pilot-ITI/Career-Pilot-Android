package com.iti.careerpilot.ats.presentation.scoring.view.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.iti.careerpilot.ats.presentation.components.AtsWorkspaceLoadingContent
import com.iti.careerpilot.ats.presentation.scoring.state.ScoringIntent
import com.iti.careerpilot.ats.presentation.scoring.state.ScoringUiState
import com.iti.careerpilot.ats.presentation.scoring.uimodel.ScoringPhase
import com.iti.careerpilot.ats.presentation.util.UiStateProvider
import com.iti.careerpilot.ats.presentation.util.rememberUiStateValue

@Composable
fun ScoringBody(
    stateProvider: UiStateProvider<ScoringUiState>,
    onIntent: (ScoringIntent) -> Unit,
    onOpenJob: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val phase by rememberUiStateValue(stateProvider) { state ->
        when {
            state.isLoading -> ScoringPhase.LOADING
            state.workspace == null || state.score == null -> ScoringPhase.ERROR
            else -> ScoringPhase.CONTENT
        }
    }

    when (phase) {
        ScoringPhase.LOADING -> AtsWorkspaceLoadingContent(modifier = modifier)
        ScoringPhase.ERROR -> ScoringErrorContent(
            stateProvider = stateProvider,
            onIntent = onIntent,
            modifier = modifier,
        )
        ScoringPhase.CONTENT -> ScoringContent(
            stateProvider = stateProvider,
            onIntent = onIntent,
            onOpenJob = onOpenJob,
            modifier = modifier,
        )
    }
}
