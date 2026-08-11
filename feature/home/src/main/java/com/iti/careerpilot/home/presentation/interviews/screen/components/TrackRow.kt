package com.iti.careerpilot.home.presentation.interviews.screen.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.iti.careerpilot.core.designsystem.CareerPilotPalette
import com.iti.careerpilot.core.designsystem.components.CareerPilotCard
import com.iti.careerpilot.home.R
import com.iti.careerpilot.home.domain.model.InterviewTrack

@Composable
fun TrackRow(
    track: InterviewTrack,
    onPracticeClick: () -> Unit,
    onLessonClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    CareerPilotCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = track.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (track.description.isNotBlank()) {
                Text(
                    text = track.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = CareerPilotPalette.gray600,
                )
            }
            Button(
                onClick = onPracticeClick,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Spacer(
                    Modifier.weight(1f)
                )
                Text(
                    text = stringResource(R.string.start_interview)
                )
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = stringResource(R.string.interviews_open),
                        tint = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            }
            TextButton (
                onClick = onLessonClick,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.see_lessons)
                )
            }
        }
    }
}
