package com.iti.careerpilot.home.presentation.home.screen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.iti.careerpilot.core.designsystem.CareerPilotPalette
import com.iti.careerpilot.core.designsystem.CareerPilotShapes
import com.iti.careerpilot.core.designsystem.Dimens
import com.iti.careerpilot.core.designsystem.components.CareerPilotCard
import com.iti.careerpilot.core.designsystem.components.rememberSessionTimestamp
import com.iti.careerpilot.home.R
import com.iti.careerpilot.core.interviews.domain.model.InterviewSession

@Composable
fun SectionHeader(
    title: String,
    actionLabel: String?,
    onActionClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f),
        )
        if (actionLabel != null && onActionClick != null) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(onClick = onActionClick),
            ) {
                Text(
                    text = actionLabel,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp)
                )
            }
        }
    }
}

@Composable
fun SessionRow(
    session: InterviewSession,
    onClick: () -> Unit,
    onResume: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val rowClick = when {
        session.isCompleted -> onClick
        session.isResumable -> onResume
        else -> null
    }

    CareerPilotCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .then(if (rowClick != null) Modifier.clickable(onClick = rowClick) else Modifier)
                .padding(Dimens.CardPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceL),
        ) {
            ScoreBadge(score = session.score)

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = session.trackName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = stringResource(
                        R.string.home_session_meta,
                        rememberSessionTimestamp(session.occurredAt),
                        session.durationMinutes,
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = CareerPilotPalette.gray600,
                )
            }

            if (session.isResumable) {
                ContinueChip(onClick = onResume)
            } else if (session.isCompleted) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = CareerPilotPalette.gray400,
                )
            }
        }
    }
}

@Composable
private fun ContinueChip(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(CareerPilotShapes.small)
            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f))
            .clickable(onClick = onClick)
            .padding(horizontal = Dimens.SpaceL, vertical = Dimens.SpaceS),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceXS),
    ) {
        Text(
            text = stringResource(R.string.home_session_continue),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.secondary,
        )
        Icon(
            imageVector = Icons.Filled.PlayArrow,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(18.dp),
        )
    }
}

@Composable
private fun ScoreBadge(score: Int?) {
    val accent = when {
        score == null -> CareerPilotPalette.gray400
        score >= HIGH_SCORE -> CareerPilotPalette.green
        score >= MID_SCORE -> CareerPilotPalette.yellow
        else -> MaterialTheme.colorScheme.error
    }

    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CareerPilotShapes.small)
            .background(accent.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = score?.toString() ?: stringResource(R.string.home_session_unscored),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = accent,
        )
    }
}

private const val HIGH_SCORE = 80
private const val MID_SCORE = 60
