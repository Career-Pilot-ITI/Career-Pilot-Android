package com.iti.careerpilot.quiz.presentation.screen.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.iti.careerpilot.core.designsystem.CareerPilotPalette
import com.iti.careerpilot.core.designsystem.CareerPilotShapes
import com.iti.careerpilot.core.designsystem.Dimens
import com.iti.careerpilot.core.designsystem.components.CareerPilotCard
import com.iti.careerpilot.quiz.R
import com.iti.careerpilot.quiz.domain.model.StudyTopic

@Composable
fun TopicItem(
    topic: StudyTopic,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val statusColor = if (topic.isCompleted) {
        MaterialTheme.colorScheme.secondary
    } else {
        MaterialTheme.colorScheme.primary
    }

    CareerPilotCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .padding(Dimens.CardPadding)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = topic.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = topic.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = CareerPilotPalette.gray600
                )
                Spacer(modifier = Modifier.size(Dimens.SpaceS))
                LinearProgressIndicator(
                    progress = { topic.progress / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(CareerPilotShapes.small),
                    color = statusColor,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(Dimens.SpaceM))

            TopicStatusBadge(
                progress = topic.progress,
                isCompleted = topic.isCompleted,
                color = statusColor
            )
        }
    }
}

@Composable
private fun TopicStatusBadge(
    progress: Int,
    isCompleted: Boolean,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.quiz_percentage_format, progress),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = if (isCompleted) color else MaterialTheme.colorScheme.onSurface
        )
        if (isCompleted) {
            Spacer(modifier = Modifier.width(Dimens.SpaceS))
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}