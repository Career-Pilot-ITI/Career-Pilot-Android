package com.iti.careerpilot.ats.presentation.optimizedcv.view.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AssistChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.iti.careerpilot.ats.R
import com.iti.careerpilot.core.designsystem.components.CareerPilotCard

@Composable
internal fun OptimizedTextCard(
    text: String,
    modifier: Modifier = Modifier,
) {
    CareerPilotCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.ats_optimized_text),
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = text,
                modifier = Modifier.padding(top = 10.dp),
            )
        }
    }
}

@Composable
internal fun RecommendedTracksCard(
    tracks: List<String>,
    modifier: Modifier = Modifier,
) {
    CareerPilotCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.ats_recommended_tracks),
                fontWeight = FontWeight.Bold,
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                tracks.forEach { track ->
                    AssistChip(
                        onClick = {},
                        label = { Text(track) },
                    )
                }
            }
        }
    }
}
