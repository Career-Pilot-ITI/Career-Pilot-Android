package com.iti.careerpilot.reports.presentation.screen.details.view.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.iti.careerpilot.core.designsystem.Dimens
import com.iti.careerpilot.reports.presentation.screen.components.performanceMetricLabel
import com.iti.careerpilot.reports.presentation.screen.details.uimodels.PerformanceMetricUiModel
import com.iti.careerpilot.reports.presentation.screen.details.util.metricColor

@Composable
fun PerformanceMetricChip(metric: PerformanceMetricUiModel) {
    val color = metricColor(metric.score)
    Surface(
        shape = RoundedCornerShape(Dimens.SpaceL),
        color = color.copy(alpha = 0.10f),
        contentColor = color,
    ) {
        Text(
            text = "${performanceMetricLabel(metric.type)} ${metric.score}",
            modifier = Modifier.padding(horizontal = Dimens.SpaceM, vertical = Dimens.SpaceS),
            style = MaterialTheme.typography.labelSmall,
        )
    }
}