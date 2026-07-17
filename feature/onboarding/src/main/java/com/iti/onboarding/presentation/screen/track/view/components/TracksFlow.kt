package com.iti.onboarding.presentation.screen.track.view.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.iti.onboarding.domain.model.Track

@Composable
fun TracksFlow(
    tracks: List<Track>,
    onTrackClick: (track: Track) -> Unit,
    modifier: Modifier = Modifier,
    selectedTrack: Track?= null,
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        tracks.forEach { track ->
            TrackChip(
                track = track,
                onClick = {
                    onTrackClick(track)
                },
                isSelected = track == selectedTrack
            )
        }
    }
}