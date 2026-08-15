package com.iti.careerpilot.ats.presentation.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.iti.careerpilot.ats.presentation.scoring.uimodel.SkillPaletteData
import com.iti.careerpilot.ats.presentation.scoring.uimodel.SkillStatus
import com.iti.careerpilot.core.designsystem.CareerPilotTheme

@Composable
fun skillPalette(status: SkillStatus): SkillPaletteData {
    val colors = CareerPilotTheme.extendedColors
    return when (status) {
        SkillStatus.MATCHED -> SkillPaletteData(
            icon = Icons.Outlined.Check,
            containerColor = colors.successContainer,
            contentColor = colors.onSuccessContainer,
        )
        SkillStatus.REQUIRED_MISSING -> SkillPaletteData(
            icon = Icons.Outlined.Close,
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
        )
        SkillStatus.PREFERRED_MISSING -> SkillPaletteData(
            icon = Icons.Outlined.ErrorOutline,
            containerColor = colors.warningContainer,
            contentColor = colors.onWarningContainer,
        )
    }
}