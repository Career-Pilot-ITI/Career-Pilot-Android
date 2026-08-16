package com.iti.careerpilot.ats.presentation.scoring.view.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.iti.careerpilot.ats.R
import com.iti.careerpilot.ats.presentation.components.AtsWorkspaceErrorContent
import com.iti.careerpilot.ats.presentation.scoring.state.ScoringIntent
import com.iti.careerpilot.ats.presentation.scoring.state.ScoringUiState
import com.iti.careerpilot.ats.presentation.util.UiStateProvider
import com.iti.careerpilot.ats.presentation.util.rememberUiStateValue
import com.iti.careerpilot.core.designsystem.components.ButtonVariant
import com.iti.careerpilot.core.designsystem.components.CareerPilotButton

@Composable
fun ScoringErrorContent(
    stateProvider: UiStateProvider<ScoringUiState>,
    onIntent: (ScoringIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val error by rememberUiStateValue(stateProvider) { it.error }
    val hasInsufficientCoins by rememberUiStateValue(stateProvider) { it.hasInsufficientCoins }
    Column(modifier = modifier) {
        AtsWorkspaceErrorContent(
            error = error,
            onRetry = { onIntent(ScoringIntent.Retry) },
            modifier = Modifier.weight(1f),
        )
        if (hasInsufficientCoins) {
            CareerPilotButton(
                text = stringResource(R.string.ats_get_coins),
                onClick = { onIntent(ScoringIntent.OpenCoins) },
                variant = ButtonVariant.OUTLINE,
                modifier = Modifier.padding(20.dp),
            )
        }
    }
}