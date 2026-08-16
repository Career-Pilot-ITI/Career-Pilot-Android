package com.iti.careerpilot.ats.presentation.scoring.view.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.iti.careerpilot.ats.R
import com.iti.careerpilot.ats.presentation.scoring.state.ScoringIntent
import com.iti.careerpilot.ats.presentation.scoring.state.ScoringUiState
import com.iti.careerpilot.ats.presentation.util.UiStateProvider
import com.iti.careerpilot.ats.presentation.util.rememberUiStateValue
import com.iti.careerpilot.core.access.FeaturePricingMap
import com.iti.careerpilot.core.designsystem.components.ButtonVariant
import com.iti.careerpilot.core.designsystem.components.CareerPilotButton
import com.iti.careerpilot.core.designsystem.components.FeaturePricingBadge
import com.iti.core.model.FeatureKey

@Composable
fun OptimizeCvActionItem(
    stateProvider: UiStateProvider<ScoringUiState>,
    onIntent: (ScoringIntent) -> Unit,
) {
    val hasInsufficientCoins by rememberUiStateValue(stateProvider) { it.hasInsufficientCoins }
    val isStartingOptimization by rememberUiStateValue(stateProvider) {
        it.isStartingOptimization
    }
    val optimizationError by rememberUiStateValue(stateProvider) { it.optimizationError }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (hasInsufficientCoins) {
            CareerPilotButton(
                text = stringResource(R.string.ats_get_coins),
                onClick = { onIntent(ScoringIntent.OpenCoins) },
                variant = ButtonVariant.OUTLINE,
            )
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CareerPilotButton(
                    text = stringResource(
                        if (isStartingOptimization) {
                            R.string.ats_cv_optimization_starting
                        } else {
                            R.string.ats_optimize_cv
                        },
                    ),
                    onClick = { onIntent(ScoringIntent.OptimizeCv) },
                    enabled = !isStartingOptimization,
                    modifier = Modifier.weight(1f),
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Outlined.Lightbulb,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                        )
                    },
                )
                FeaturePricingBadge(
                    coinCost = FeaturePricingMap.coinCost(FeatureKey.CvAiAnalysis),
                    compact = true,
                )
            }
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
}