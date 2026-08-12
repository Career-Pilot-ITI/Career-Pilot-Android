package com.iti.careerpilot.ats.presentation.scoring.view.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.iti.careerpilot.ats.R
import com.iti.careerpilot.ats.presentation.scoring.state.ScoringAction
import com.iti.careerpilot.ats.presentation.scoring.state.ScoringUiState
import com.iti.careerpilot.core.designsystem.components.CareerPilotButton

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
