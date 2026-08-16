package com.iti.careerpilot.ats.presentation.optimizedcv.view.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.iti.careerpilot.ats.R
import com.iti.careerpilot.ats.domain.model.CvOptimizationSection
import com.iti.careerpilot.ats.presentation.optimizedcv.state.OptimizedCvIntent
import com.iti.careerpilot.ats.presentation.optimizedcv.state.OptimizedCvUiState
import com.iti.careerpilot.ats.presentation.util.UiStateProvider
import com.iti.careerpilot.ats.presentation.util.rememberUiStateValue
import com.iti.common.util.UIText
import com.iti.careerpilot.core.designsystem.components.CareerPilotButton
import kotlinx.collections.immutable.ImmutableList

@Composable
internal fun OptimizedCvContent(
    stateProvider: UiStateProvider<OptimizedCvUiState>,
    onIntent: (OptimizedCvIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val content by rememberUiStateValue(stateProvider) {
        OptimizedCvContentState(
            error = it.error,
            sections = it.sections,
            recommendedTracks = it.recommendedTracks,
            coinCost = it.coinCost,
        )
    }
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        content.error?.let { error ->
            item {
                Text(
                    text = error.asString(),
                    color = MaterialTheme.colorScheme.error,
                )
            }
            item {
                CareerPilotButton(
                    text = stringResource(R.string.ats_retry),
                    onClick = { onIntent(OptimizedCvIntent.Retry) },
                )
            }
        }
        if (content.error == null) {
            if (content.sections.isEmpty()) {
                item { Text(stringResource(R.string.ats_cv_optimization_no_sections)) }
            } else {
                item {
                    Text(
                        text = stringResource(R.string.ats_section_breakdown),
                        fontWeight = FontWeight.Bold,
                    )
                }
                itemsIndexed(
                    items = content.sections,
                    key = { index, section -> "${section.name}_$index" },
                ) { _, section ->
                    OptimizationSectionCard(section = section)
                }
            }
            if (content.recommendedTracks.isNotEmpty()) {
                item { RecommendedTracksCard(tracks = content.recommendedTracks) }
            }
            content.coinCost?.let { cost ->
                item { Text(pluralStringResource(R.plurals.ats_coins_used, cost, cost)) }
            }
        }
    }
}

@Immutable
private data class OptimizedCvContentState(
    val error: UIText?,
    val sections: ImmutableList<CvOptimizationSection>,
    val recommendedTracks: ImmutableList<String>,
    val coinCost: Int?,
)
