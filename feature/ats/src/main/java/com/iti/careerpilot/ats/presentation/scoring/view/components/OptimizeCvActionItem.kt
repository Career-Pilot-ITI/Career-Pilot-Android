package com.iti.careerpilot.ats.presentation.scoring.view.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.iti.careerpilot.ats.R
import com.iti.careerpilot.ats.presentation.scoring.state.ScoringAction
import com.iti.careerpilot.ats.presentation.scoring.state.ScoringUiState
import com.iti.careerpilot.ats.presentation.util.UiStateProvider
import com.iti.careerpilot.ats.presentation.util.rememberUiStateValue
import com.iti.careerpilot.core.designsystem.components.ButtonVariant
import com.iti.careerpilot.core.designsystem.components.CareerPilotButton


@Composable
fun OptimizeCvActionItem(
    stateProvider: UiStateProvider<ScoringUiState>,
    onAction: (ScoringAction) -> Unit,
) {
    val hasInsufficientCoins by rememberUiStateValue(stateProvider) { it.hasInsufficientCoins }
    val isStartingOptimization by rememberUiStateValue(stateProvider) {
        it.isStartingOptimization
    }
    val optimizationError by rememberUiStateValue(stateProvider) { it.optimizationError }

    if (hasInsufficientCoins) {
        CareerPilotButton(
            text = stringResource(R.string.ats_get_coins),
            onClick = { onAction(ScoringAction.OpenCoins) },
            variant = ButtonVariant.OUTLINE,
        )
    } else {
        CareerPilotButton(
            text = stringResource(
                if (isStartingOptimization) {
                    R.string.ats_cv_optimization_starting
                } else {
                    R.string.ats_optimize_cv
                },
            ),
            onClick = { onAction(ScoringAction.OptimizeCv) },
            enabled = !isStartingOptimization,
            leadingContent = {
                Icon(
                    painter = painterResource(R.drawable.ic_lightbulb_outline),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
            },
        )
    }
    optimizationError?.let { error ->
        Text(
            text = error.asString(),
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 6.dp),
        )
    }
}