package com.iti.careerpilot.practicesession.presentation.resultscreen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.iti.careerpilot.bodylanguage.model.KeyMoment
import com.iti.careerpilot.bodylanguage.model.KeyMomentType
import com.iti.careerpilot.core.designsystem.Dimens
import com.iti.careerpilot.core.designsystem.components.CareerPilotCard

@Composable
fun KeyMomentsTimeline(
    keyMoments: List<KeyMoment>,
    modifier: Modifier = Modifier,
) {
    if (keyMoments.isEmpty()) return

    CareerPilotCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(Dimens.SpaceXL)) {
            Text(
                text = "Key Moments",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
            )
            Spacer(modifier = Modifier.height(Dimens.SpaceL))

            keyMoments.take(10).forEach { moment ->
                KeyMomentItem(moment)
                Spacer(modifier = Modifier.height(Dimens.SpaceS))
            }
        }
    }
}

@Composable
private fun KeyMomentItem(moment: KeyMoment) {
    val (label, color) = when (moment.type) {
        KeyMomentType.LOOKED_AWAY -> "Looked away" to MaterialTheme.colorScheme.error
        KeyMomentType.SLOUCHED -> "Slouched" to MaterialTheme.colorScheme.error
        KeyMomentType.HAND_TO_FACE -> "Hand to face" to MaterialTheme.colorScheme.tertiary
        KeyMomentType.FIDGETED -> "Fidgeting" to MaterialTheme.colorScheme.tertiary
        KeyMomentType.FACE_LOST -> "Face not visible" to MaterialTheme.colorScheme.error
        KeyMomentType.SMILED -> "Smiled 😊" to MaterialTheme.colorScheme.primary
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color),
        )
        Spacer(modifier = Modifier.width(Dimens.SpaceM))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = formatTimestamp(moment.timestampMs),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        moment.durationMs?.let { dur ->
            Spacer(modifier = Modifier.width(Dimens.SpaceS))
            Text(
                text = "${dur / 1000}s",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun formatTimestamp(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
