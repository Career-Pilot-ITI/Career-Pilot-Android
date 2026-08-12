package com.iti.careerpilot.ats.presentation.scoring.view.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.iti.careerpilot.ats.R
import com.iti.careerpilot.ats.presentation.scoring.state.ScoringAction
import com.iti.careerpilot.ats.presentation.scoring.state.ScoringUiState
import com.iti.careerpilot.core.designsystem.components.ButtonVariant
import com.iti.careerpilot.core.designsystem.components.CareerPilotButton

@Composable
internal fun BeforeScoreContent(
    state: ScoringUiState,
    onAction: (ScoringAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        state.workspace?.let { workspace ->
            Text(
                text = workspace.job.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            if (workspace.job.companyName.isNotBlank()) {
                Text(
                    text = workspace.job.companyName,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        if (state.wasInterrupted) {
            Text(
                text = stringResource(R.string.ats_scoring_interrupted),
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 16.dp),
            )
        }
        state.error?.let { error ->
            Text(
                text = error.asString(),
                color = MaterialTheme.colorScheme.error,
            )
        }
        if (state.hasInsufficientCoins) {
            CareerPilotButton(
                text = stringResource(R.string.ats_get_coins),
                onClick = { onAction(ScoringAction.OpenCoins) },
                variant = ButtonVariant.OUTLINE,
                modifier = Modifier.padding(top = 12.dp),
            )
        }
        CareerPilotButton(
            text = stringResource(R.string.ats_score_cv),
            onClick = { onAction(ScoringAction.RequestScore) },
            modifier = Modifier.padding(top = 20.dp),
        )
    }
}

@Composable
internal fun ScoringErrorContent(
    state: ScoringUiState,
    onAction: (ScoringAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(state.error?.asString() ?: stringResource(R.string.ats_unknown_error))
        CareerPilotButton(
            text = stringResource(R.string.ats_retry),
            onClick = { onAction(ScoringAction.RetryWorkspace) },
            modifier = Modifier.padding(top = 16.dp),
        )
    }
}
