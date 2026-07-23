package com.iti.careerpilot.features.paywall.presentation.view.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import com.iti.careerpilot.core.designsystem.CareerPilotTypography
import com.iti.careerpilot.core.designsystem.Dimens
import com.iti.careerpilot.core.designsystem.components.CareerPilotCard
import com.iti.careerpilot.payment.R

@Composable
fun MonthlyLimitHeader(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(Dimens.LockIconBoxSize)
            .background(
                Brush.linearGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.secondary
                    )
                ),
                CircleShape
            )
            .padding(Dimens.SpaceXS)
            .background(MaterialTheme.colorScheme.surface, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Rounded.Lock,
            contentDescription = null,
            modifier = Modifier.size(Dimens.SpaceXXXXL),
            tint = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun MonthlyLimitProgressCard(
    usedSessions: Int,
    maxSessions: Int,
    resetDate: String,
    modifier: Modifier = Modifier
) {
    CareerPilotCard(
        elevation = Dimens.SpaceS,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.SpaceL)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(id = R.string.paywall_free_sessions, usedSessions, maxSessions),
                    style = CareerPilotTypography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(Dimens.SpaceM))

            LinearProgressIndicator(
                progress = { if (maxSessions > 0) usedSessions.toFloat() / maxSessions else 0f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimens.SpaceS),
                color = MaterialTheme.colorScheme.error,
                trackColor = MaterialTheme.colorScheme.surface,
            )

            Spacer(modifier = Modifier.height(Dimens.SpaceM))

            Text(
                text = stringResource(id = R.string.paywall_resets_date, resetDate),
                style = CareerPilotTypography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
