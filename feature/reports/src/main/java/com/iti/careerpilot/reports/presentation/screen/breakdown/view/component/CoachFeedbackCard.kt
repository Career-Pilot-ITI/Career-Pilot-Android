package com.iti.careerpilot.reports.presentation.screen.breakdown.view.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.iti.careerpilot.core.designsystem.Dimens
import com.iti.careerpilot.core.designsystem.components.CareerPilotCard
import com.iti.careerpilot.reports.R

@Composable
fun CoachFeedbackCard(
    feedback: String,
    modifier: Modifier = Modifier,
) {
    CareerPilotCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(Dimens.CardPadding)) {
            Text(
                text = stringResource(R.string.reports_coach_feedback),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = feedback,
                modifier = Modifier.padding(top = Dimens.SpaceXS),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.66f),
            )
        }
    }
}