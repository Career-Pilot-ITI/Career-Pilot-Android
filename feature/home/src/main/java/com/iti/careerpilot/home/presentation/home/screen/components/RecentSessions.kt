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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
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
import com.iti.careerpilot.home.R
import com.iti.careerpilot.home.domain.model.InterviewSession
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

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
            Text(
                text = actionLabel,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable(onClick = onActionClick),
            )
        }
    }
}

@Composable
fun SessionRow(
    session: InterviewSession,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    CareerPilotCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .clickable(onClick = onClick)
                .padding(Dimens.CardPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceL),
        ) {
            ScoreBadge(score = session.overallScore)

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

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = CareerPilotPalette.gray400,
            )
        }
    }
}

@Composable
private fun ScoreBadge(score: Int?) {
    // Colour tracks the same bands as the headline score card.
    val accent = when {
        score == null -> CareerPilotPalette.gray400
        score >= HIGH_SCORE -> CareerPilotPalette.green
        score >= MID_SCORE -> CareerPilotPalette.yellow
        else -> MaterialTheme.colorScheme.error
    }

    Box(
        modifier = Modifier
            .size(BADGE_SIZE)
            .clip(CareerPilotShapes.small)
            .background(accent.copy(alpha = BADGE_BACKGROUND_ALPHA)),
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

/**
 * Renders a session time as "Today, 2:14 PM" / "Yesterday, 10:30 AM" / "Mon, 9:00 AM".
 *
 * The instant was built by treating the server's offset-less timestamp as UTC; here it is
 * converted into the device's zone so the label matches the user's own clock.
 */
@Composable
private fun rememberSessionTimestamp(instant: Instant?): String {
    if (instant == null) return stringResource(R.string.home_session_unscored)

    val dateTime = instant.atZone(ZoneId.systemDefault())
    val sessionDate = dateTime.toLocalDate()
    val daysApart = LocalDate.now().toEpochDay() - sessionDate.toEpochDay()

    val time = TIME_FORMATTER.format(LocalTime.of(dateTime.hour, dateTime.minute))

    return when (daysApart) {
        0L -> stringResource(R.string.home_session_today, time)
        1L -> stringResource(R.string.home_session_yesterday, time)
        else -> "${DAY_FORMATTER.format(sessionDate)}, $time"
    }
}

private val TIME_FORMATTER: DateTimeFormatter =
    DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
private val DAY_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE")

private const val HIGH_SCORE = 80
private const val MID_SCORE = 60
private const val BADGE_BACKGROUND_ALPHA = 0.15f
private val BADGE_SIZE = 48.dp
