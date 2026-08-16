package com.iti.careerpilot.ats.presentation.coverletter.view.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.iti.careerpilot.ats.R
import com.iti.careerpilot.ats.presentation.components.RecommendationsCard
import com.iti.careerpilot.ats.presentation.coverletter.state.CoverLetterIntent
import com.iti.careerpilot.ats.presentation.coverletter.state.CoverLetterUiState
import com.iti.careerpilot.ats.presentation.util.UiStateProvider
import com.iti.careerpilot.ats.presentation.util.rememberUiStateValue
import com.iti.careerpilot.core.designsystem.components.ButtonVariant
import com.iti.careerpilot.core.designsystem.components.CareerPilotButton

@Composable
internal fun CoverLetterContent(
    stateProvider: UiStateProvider<CoverLetterUiState>,
    onIntent: (CoverLetterIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val error by rememberUiStateValue(stateProvider) { it.error }
    val wasInterrupted by rememberUiStateValue(stateProvider) { it.wasInterrupted }
    val hasContent by rememberUiStateValue(stateProvider) { it.editedValue.isNotBlank() }
    val approachTips by rememberUiStateValue(stateProvider) { it.approachTips }
    val coinCost by rememberUiStateValue(stateProvider) { it.coinCost }
    val hasInsufficientCoins by rememberUiStateValue(stateProvider) { it.hasInsufficientCoins }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        error?.let { currentError ->
            item {
                Text(
                    text = currentError.asString(),
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
        if (wasInterrupted) {
            item {
                Text(
                    text = stringResource(R.string.ats_generation_interrupted),
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
        if (!hasContent) {
            if (error != null || wasInterrupted) {
                item {
                    CareerPilotButton(
                        text = stringResource(R.string.ats_retry),
                        onClick = { onIntent(CoverLetterIntent.Retry) },
                    )
                }
            }
        } else {
            item {
                GeneratedCoverLetterCard(
                    stateProvider = stateProvider,
                    onIntent = onIntent,
                )
            }
            if (approachTips.isNotEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.ats_approach_tips),
                        fontWeight = FontWeight.Bold,
                    )
                }
                item { RecommendationsCard(values = approachTips) }
            }
            coinCost?.let { cost ->
                item { Text(pluralStringResource(R.plurals.ats_coins_used, cost, cost)) }
            }
            item {
                CareerPilotButton(
                    text = stringResource(R.string.ats_send_email),
                    onClick = { onIntent(CoverLetterIntent.Email) },
                )
            }
        }
        if (hasInsufficientCoins) {
            item {
                CareerPilotButton(
                    text = stringResource(R.string.ats_get_coins),
                    onClick = { onIntent(CoverLetterIntent.OpenCoins) },
                    variant = ButtonVariant.OUTLINE,
                )
            }
        }
    }
}
