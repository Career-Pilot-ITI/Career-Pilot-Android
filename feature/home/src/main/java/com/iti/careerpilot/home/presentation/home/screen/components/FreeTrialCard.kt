package com.iti.careerpilot.home.presentation.home.screen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp.Companion.Hairline
import com.iti.careerpilot.core.designsystem.CareerPilotPalette
import com.iti.careerpilot.core.designsystem.CareerPilotShapes
import com.iti.careerpilot.core.designsystem.Dimens
import com.iti.careerpilot.home.R
import com.iti.careerpilot.home.presentation.home.TrialUiState

@Composable
fun FreeTrialCard(
    trial: TrialUiState,
    onUpgradeClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(CareerPilotShapes.medium)
            .background(MaterialTheme.colorScheme.primary)
            .padding(Dimens.SpaceXL),
        verticalArrangement = Arrangement.spacedBy(Dimens.SpaceM),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.home_free_trial),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                )
                Text(
                    text = pluralStringResource(
                        R.plurals.home_free_sessions_remaining,
                        trial.sessionsRemaining,
                        trial.sessionsRemaining,
                    ),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }

            Text(
                text = stringResource(R.string.home_upgrade),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.22f))
                    .clickable(onClick = onUpgradeClick)
                    .padding(horizontal = Dimens.SpaceXL, vertical = Dimens.SpaceM),
            )
        }

        LinearProgressIndicator(
            progress = { trial.progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(Dimens.SpaceS)
                .clip(CircleShape),
            color = MaterialTheme.colorScheme.onPrimary,
            trackColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.3f),
            gapSize = Hairline,
        )

        Text(
            text = stringResource(
                R.string.home_free_sessions_used,
                trial.sessionsUsed,
                trial.totalFreeSessions,
            ),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
        )
    }
}
