package com.iti.careerpilot.reports.presentation.screen.details.util

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.iti.careerpilot.core.designsystem.CareerPilotPalette

@Composable
fun metricColor(score: Int): Color = when {
    score >= 80 -> CareerPilotPalette.green
    score >= 60 -> CareerPilotPalette.yellow
    else -> MaterialTheme.colorScheme.error
}
