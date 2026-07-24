package com.iti.careerpilot.reports.presentation.screen.history.view.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.iti.careerpilot.core.designsystem.CareerPilotShapes
import com.iti.careerpilot.core.designsystem.Dimens
import com.iti.careerpilot.core.designsystem.components.CareerPilotCard
import com.iti.careerpilot.reports.R
import com.iti.careerpilot.reports.presentation.screen.components.localizedSessionDate
import com.iti.careerpilot.reports.presentation.screen.history.uimodels.SessionSummaryUiModel

@Composable
fun SessionHistoryCard(
    session: SessionSummaryUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val date = localizedSessionDate(session.completedAt)
    val minutes = pluralStringResource(
        R.plurals.reports_minutes,
        session.durationMinutes,
        session.durationMinutes,
    )
    val questions = pluralStringResource(
        R.plurals.reports_questions,
        session.questionCount,
        session.questionCount,
    )
    val dateAndDuration = stringResource(R.string.reports_summary_separator, date, minutes)
    val supportingText = stringResource(R.string.reports_summary_separator, dateAndDuration, questions)
    val description = stringResource(
        R.string.reports_open_session,
        session.category,
        session.score,
    )

    CareerPilotCard(
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = description }
            .clip(CareerPilotShapes.medium)
            .clickable(role = Role.Button, onClick = onClick),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.CardPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceM),
        ) {
            ScoreBadge(score = session.score)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = session.category,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                )
                Text(
                    text = supportingText,
                    modifier = Modifier.padding(top = Dimens.SpaceXS),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.56f),
                    maxLines = 1,
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Default.ArrowForwardIos,
                contentDescription = null,
                modifier = Modifier.size(Dimens.IconSizeM),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.42f),
            )
        }
    }
}