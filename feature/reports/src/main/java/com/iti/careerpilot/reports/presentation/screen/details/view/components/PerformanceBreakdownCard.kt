package com.iti.careerpilot.reports.presentation.screen.details.view.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.iti.careerpilot.core.designsystem.Dimens
import com.iti.careerpilot.core.designsystem.components.CareerPilotCard
import com.iti.careerpilot.reports.R
import com.iti.careerpilot.reports.presentation.screen.details.uimodels.PerformanceMetricUiModel
import kotlinx.collections.immutable.ImmutableList

@Composable
fun PerformanceBreakdownCard(
    metrics: ImmutableList<PerformanceMetricUiModel>,
    modifier: Modifier = Modifier,
) {
    CareerPilotCard(modifier = modifier.fillMaxWidth(), elevation = 2.dp) {
        Column(modifier = Modifier.padding(Dimens.CardPadding)) {
            Text(
                text = stringResource(R.string.reports_performance_breakdown),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            )
            RadarChart(
                metrics = metrics,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceS),
                verticalArrangement = Arrangement.spacedBy(Dimens.SpaceS),
            ) {
                metrics.forEach { metric ->
                    PerformanceMetricChip(metric)
                }
            }
        }
    }
}
