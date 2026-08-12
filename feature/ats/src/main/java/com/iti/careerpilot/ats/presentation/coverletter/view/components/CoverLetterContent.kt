package com.iti.careerpilot.ats.presentation.coverletter.view.components

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
import com.iti.careerpilot.ats.presentation.coverletter.state.CoverLetterAction
import com.iti.careerpilot.ats.presentation.coverletter.state.CoverLetterUiState
import com.iti.careerpilot.core.designsystem.components.ButtonVariant
import com.iti.careerpilot.core.designsystem.components.CareerPilotButton

@Composable
internal fun CoverLetterContent(
    state: CoverLetterUiState,
    onAction: (CoverLetterAction) -> Unit,
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
        if (state.editedValue.isBlank()) {
            item {
                CareerPilotButton(
                    text = stringResource(R.string.ats_generate_cover_letter),
                    onClick = { onAction(CoverLetterAction.RequestGeneration) },
                )
            }
        } else {
            item {
                GeneratedCoverLetterCard(
                    state = state,
                    onAction = onAction,
                )
            }
            state.approachTips?.takeIf(String::isNotBlank)?.let { tips ->
                item { ApproachTipsCard(tips = tips) }
            }
            state.coinCost?.let { cost ->
                item { Text(pluralStringResource(R.plurals.ats_coins_used, cost, cost)) }
            }
            item {
                CareerPilotButton(
                    text = stringResource(R.string.ats_send_email),
                    onClick = { onAction(CoverLetterAction.Email) },
                )
            }
        }
        if (state.hasInsufficientCoins) {
            item {
                CareerPilotButton(
                    text = stringResource(R.string.ats_get_coins),
                    onClick = { onAction(CoverLetterAction.OpenCoins) },
                    variant = ButtonVariant.OUTLINE,
                )
            }
        }
    }
}
