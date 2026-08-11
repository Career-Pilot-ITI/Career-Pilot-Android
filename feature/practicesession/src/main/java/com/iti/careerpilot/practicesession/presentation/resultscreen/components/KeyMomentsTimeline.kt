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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.iti.careerpilot.core.designsystem.Dimens
import com.iti.careerpilot.core.designsystem.components.CareerPilotCard
import com.iti.careerpilot.practicesession.R
import com.iti.careerpilot.practicesession.presentation.practicescreen.screen.util.formatDuration
import com.iti.core.model.bodylanguage.KeyMoment
import com.iti.core.model.bodylanguage.KeyMomentType

@Composable
fun KeyMomentsTimeline(
    keyMoments: List<KeyMoment>,
    modifier: Modifier = Modifier,
) {
    if (keyMoments.isEmpty()) return

    CareerPilotCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(Dimens.SpaceXL)) {
            Text(
                text = stringResource(R.string.key_moments),
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
        KeyMomentType.EYE_CONTACT_LOST -> stringResource(R.string.moment_looked_away) to MaterialTheme.colorScheme.error
        KeyMomentType.SLOUCH_START -> stringResource(R.string.moment_slouched) to MaterialTheme.colorScheme.error
        KeyMomentType.HAND_FIDGET_SPIKE -> stringResource(R.string.moment_hand_fidget) to MaterialTheme.colorScheme.tertiary
        KeyMomentType.HAND_TO_FACE_TOUCH -> stringResource(R.string.moment_hand_fidget) to MaterialTheme.colorScheme.tertiary
        KeyMomentType.FACE_LOST -> stringResource(R.string.moment_face_lost) to MaterialTheme.colorScheme.error
        KeyMomentType.SMILE_PEAK -> stringResource(R.string.moment_smiled) to MaterialTheme.colorScheme.primary
        KeyMomentType.POSTURE_SHIFT -> stringResource(R.string.moment_posture_shift) to MaterialTheme.colorScheme.secondary
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
            text = formatDuration(moment.timestampMs),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (moment.durationMs > 0) {
            Spacer(modifier = Modifier.width(Dimens.SpaceS))
            Text(
                text = stringResource(R.string.seconds_short, moment.durationMs / 1000),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
