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
fun PracticeActionItem(
    stateProvider: UiStateProvider<ScoringUiState>,
    onAction: (ScoringAction) -> Unit,
) {
    val trackId by rememberUiStateValue(stateProvider) { it.trackId }
    CareerPilotButton(
        text = stringResource(R.string.ats_start_practice_for_job),
        onClick = { onAction(ScoringAction.StartPractice) },
        enabled = trackId != null,
        variant = ButtonVariant.OUTLINE,
        leadingContent = {
            Icon(
                painter = painterResource(R.drawable.ic_record_outline),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
        },
    )
    if (trackId == null) {
        Text(
            text = stringResource(R.string.ats_practice_track_required),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 6.dp),
        )
    }
}