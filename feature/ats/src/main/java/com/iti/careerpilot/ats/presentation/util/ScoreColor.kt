package com.iti.careerpilot.ats.presentation.util

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.iti.careerpilot.core.designsystem.CareerPilotTheme


@Composable
internal fun scoreColor(score: Int): Color = when (score) {
    in 0..59 -> MaterialTheme.colorScheme.error
    in 60..79 -> CareerPilotTheme.extendedColors.warning
    else -> CareerPilotTheme.extendedColors.success
}
