package com.iti.careerpilot.reports.presentation.screen.details.view.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.iti.careerpilot.core.designsystem.Dimens
import com.iti.careerpilot.reports.presentation.screen.components.performanceMetricLabel
import com.iti.careerpilot.reports.presentation.screen.details.uimodels.PerformanceMetricType

@Composable
fun RadarLabel(
    type: PerformanceMetricType,
    modifier: Modifier,
) {
    Text(
        text = performanceMetricLabel(type),
        modifier = modifier.padding(Dimens.SpaceXS),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
        textAlign = TextAlign.Center,
    )
}