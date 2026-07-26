package com.iti.careerpilot.reports.presentation.screen.details.view.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.iti.careerpilot.core.designsystem.Dimens
import com.iti.careerpilot.core.designsystem.components.CareerPilotCard
import com.iti.careerpilot.reports.R
import com.iti.careerpilot.reports.presentation.screen.details.uimodels.CoachingSuggestionUiModel
import com.iti.careerpilot.reports.presentation.screen.details.util.suggestionIcon

@Composable
fun CoachingSuggestionCard(
    suggestion: CoachingSuggestionUiModel,
    modifier: Modifier = Modifier,
) {
    CareerPilotCard(modifier = modifier.fillMaxWidth(), elevation = 2.dp) {
        Row(
            modifier = Modifier.padding(Dimens.CardPadding),
            horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceM),
            verticalAlignment = Alignment.Top,
        ) {
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
            ) {
                Box(
                    modifier = Modifier.size(Dimens.MinimumTouchTarget),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(suggestionIcon(suggestion.impact)),
                        contentDescription = null,
                        modifier = Modifier.size(Dimens.IconSizeM),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceS),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(
                            R.string.reports_suggestion_number,
                            suggestion.ordinal,
                        ),
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    ImpactChip(suggestion.impact)
                }
                Text(
                    text = suggestion.description,
                    modifier = Modifier.padding(top = Dimens.SpaceXS),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.64f),
                )
            }
        }
    }
}
