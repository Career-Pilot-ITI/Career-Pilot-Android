package com.iti.careerpilot.ats.presentation.scoring.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iti.careerpilot.ats.R
import com.iti.careerpilot.ats.presentation.components.AtsCenteredTopBar
import com.iti.careerpilot.ats.presentation.scoring.state.ScoringAction
import com.iti.careerpilot.ats.presentation.scoring.state.ScoringEffect
import com.iti.careerpilot.ats.presentation.scoring.state.ScoringUiState
import com.iti.careerpilot.ats.presentation.scoring.view.components.JobDetailsContent
import com.iti.careerpilot.ats.presentation.scoring.view.components.ScoreContent
import com.iti.careerpilot.ats.presentation.scoring.view.components.ScoringErrorContent
import com.iti.careerpilot.ats.presentation.scoring.viewmodel.ScoringViewModel
import com.iti.careerpilot.core.designsystem.common.ObserveEvent

@Composable
fun ScoringRoot(
    workspaceId: Long,
    onBack: () -> Unit,
    openCoinsPaywall: () -> Unit,
    openCoverLetter: (Long) -> Unit,
    openOptimizedCv: (Long) -> Unit,
    openReadyToPractice: (trackId: Long, trackName: String, workspaceId: Long) -> Unit,
    openJob: (String) -> Unit,
    viewModel: ScoringViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(workspaceId) {
        viewModel.onAction(ScoringAction.Initial(workspaceId))
    }

    ObserveEvent(viewModel.effects) { effect ->
        when (effect) {
            ScoringEffect.OpenCoinsPaywall -> openCoinsPaywall()
            is ScoringEffect.OpenCoverLetter -> openCoverLetter(effect.workspaceId)
            is ScoringEffect.OpenOptimizedCv -> openOptimizedCv(effect.workspaceId)
            is ScoringEffect.OpenPractice -> openReadyToPractice(
                effect.trackId,
                effect.trackName,
                effect.workspaceId,
            )
        }
    }

    ScoringScreen(
        state = state,
        onAction = viewModel::onAction,
        onBack = onBack,
        onOpenJob = openJob,
    )
}

@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
fun ScoringScreen(
    state: ScoringUiState,
    onAction: (ScoringAction) -> Unit,
    onBack: () -> Unit,
    onOpenJob: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        AtsCenteredTopBar(
            title = stringResource(
                if (state.score == null) {
                    R.string.ats_job_description_title
                } else {
                    R.string.ats_job_match_title
                },
            ),
            onBack = onBack,
        )

        when {
            state.isLoading -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularWavyProgressIndicator()
            }

            state.workspace == null -> ScoringErrorContent(
                state = state,
                onAction = onAction,
                modifier = Modifier.fillMaxSize(),
            )

            state.score == null -> JobDetailsContent(
                workspace = requireNotNull(state.workspace),
                wasInterrupted = state.wasInterrupted,
                hasInsufficientCoins = state.hasInsufficientCoins,
                errorMessage = state.error?.asString(),
                onStartScoring = { onAction(ScoringAction.RequestScore) },
                onOpenCoins = { onAction(ScoringAction.OpenCoins) },
                onOpenJob = onOpenJob,
                modifier = Modifier.fillMaxSize(),
            )

            else -> ScoreContent(
                state = state,
                onAction = onAction,
                onOpenJob = onOpenJob,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }

    if (state.isScoreConfirmationVisible) {
        AlertDialog(
            onDismissRequest = { onAction(ScoringAction.DismissConfirmation) },
            title = { Text(stringResource(R.string.ats_confirm_scoring_title)) },
            text = { Text(stringResource(R.string.ats_paid_operation_confirmation)) },
            confirmButton = {
                TextButton(onClick = { onAction(ScoringAction.ConfirmScore) }) {
                    Text(stringResource(R.string.ats_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { onAction(ScoringAction.DismissConfirmation) }) {
                    Text(stringResource(R.string.ats_cancel))
                }
            },
        )
    }
}
