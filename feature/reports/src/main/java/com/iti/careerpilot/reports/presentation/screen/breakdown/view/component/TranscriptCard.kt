package com.iti.careerpilot.reports.presentation.screen.breakdown.view.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import com.iti.careerpilot.core.designsystem.Dimens
import com.iti.careerpilot.core.designsystem.components.CareerPilotCard
import com.iti.careerpilot.reports.R
import kotlinx.collections.immutable.ImmutableList

@Composable
fun TranscriptCard(
    transcript: String,
    fillerWords: ImmutableList<String>,
    modifier: Modifier = Modifier,
) {
    CareerPilotCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(Dimens.CardPadding)) {
            Text(
                text = stringResource(R.string.reports_transcript_snippet),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = transcript,
                modifier = Modifier.padding(top = Dimens.SpaceS),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.66f),
                fontStyle = FontStyle.Italic,
            )
            if (fillerWords.isNotEmpty()) {
                FlowRow(
                    modifier = Modifier.padding(top = Dimens.SpaceM),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceS),
                    verticalArrangement = Arrangement.spacedBy(Dimens.SpaceS),
                ) {
                    fillerWords.forEachIndexed { index, fillerWord ->
                        key("$fillerWord-$index") {
                            FillerWordChip(fillerWord)
                        }
                    }
                }
            }
        }
    }
}