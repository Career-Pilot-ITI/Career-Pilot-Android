package com.iti.careerpilot.ats.presentation.scoring.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iti.careerpilot.ats.R
import com.iti.careerpilot.ats.presentation.scoring.state.ScoringAction
import com.iti.careerpilot.ats.presentation.scoring.state.ScoringEffect
import com.iti.careerpilot.ats.presentation.scoring.state.ScoringUiState
import com.iti.careerpilot.ats.presentation.scoring.view.components.BeforeScoreContent
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
    openPracticeSession: (Long) -> Unit,
    viewModel: ScoringViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(workspaceId) {
        viewModel.loadWorkspace(workspaceId)
    }

    ObserveEvent(viewModel.effects) { effect ->
        when (effect) {
            ScoringEffect.OpenCoinsPaywall -> openCoinsPaywall()
            is ScoringEffect.OpenCoverLetter -> openCoverLetter(effect.workspaceId)
            is ScoringEffect.OpenOptimizedCv -> openOptimizedCv(effect.workspaceId)
            is ScoringEffect.OpenPractice -> openPracticeSession(effect.trackId)
        }
    }

    ScoringScreen(
        state = state,
        onAction = viewModel::onAction,
        onBack = onBack,
    )
}

@Composable
fun ScoringScreen(
    state: ScoringUiState,
    onAction: (ScoringAction) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = onBack) {
                Text(stringResource(R.string.ats_back))
            }
            Text(
                text = stringResource(R.string.ats_job_match_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )
        }

        when {
            state.isLoading -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
            state.workspace == null -> ScoringErrorContent(
                state = state,
                onAction = onAction,
                modifier = Modifier.fillMaxSize(),
            )
            state.score == null -> BeforeScoreContent(
                state = state,
                onAction = onAction,
                modifier = Modifier.fillMaxSize(),
            )
            else -> ScoreContent(
                state = state,
                onAction = onAction,
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
