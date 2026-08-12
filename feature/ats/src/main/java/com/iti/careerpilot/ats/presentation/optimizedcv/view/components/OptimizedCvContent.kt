package com.iti.careerpilot.ats.presentation.optimizedcv.view.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.iti.careerpilot.ats.R
import com.iti.careerpilot.ats.presentation.optimizedcv.state.OptimizedCvAction
import com.iti.careerpilot.ats.presentation.optimizedcv.state.OptimizedCvUiState
import com.iti.careerpilot.core.designsystem.components.ButtonVariant
import com.iti.careerpilot.core.designsystem.components.CareerPilotButton

@Composable
internal fun OptimizedCvContent(
    state: OptimizedCvUiState,
    onAction: (OptimizedCvAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        state.error?.let { error ->
            item {
                Text(
                    text = error.asString(),
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
        if (state.wasInterrupted) {
            item {
                Text(
                    text = stringResource(R.string.ats_generation_interrupted),
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
        if (state.optimizedText.isBlank()) {
            item {
                CareerPilotButton(
                    text = stringResource(R.string.ats_optimize_cv),
                    onClick = { onAction(OptimizedCvAction.RequestOptimization) },
                )
            }
        } else {
            item { OptimizedTextCard(text = state.optimizedText) }
            if (state.recommendedTracks.isNotEmpty()) {
                item { RecommendedTracksCard(tracks = state.recommendedTracks) }
            }
            state.coinCost?.let { cost ->
                item { Text(pluralStringResource(R.plurals.ats_coins_used, cost, cost)) }
            }
            item {
                CareerPilotButton(
                    text = stringResource(R.string.ats_copy_optimized_cv),
                    onClick = { onAction(OptimizedCvAction.Copy) },
                )
            }
        }
        if (state.hasInsufficientCoins) {
            item {
                CareerPilotButton(
                    text = stringResource(R.string.ats_get_coins),
                    onClick = { onAction(OptimizedCvAction.OpenCoins) },
                    variant = ButtonVariant.OUTLINE,
                )
            }
        }
    }
}
