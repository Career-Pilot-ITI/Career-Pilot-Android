package com.iti.careerpilot.ats.presentation.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.iti.careerpilot.ats.presentation.coverletter.uimodels.CoverLetterContactInfo
import com.iti.careerpilot.ats.presentation.coverletter.uimodels.CoverLetterContactInfoPalette
import com.iti.careerpilot.core.designsystem.CareerPilotTheme


@Composable
fun CoverLetterContactInfo.toPalette(): CoverLetterContactInfoPalette {
    val colors = CareerPilotTheme.extendedColors
    val colorScheme = MaterialTheme.colorScheme

    return when (this) {
        CoverLetterContactInfo.EMAIL -> CoverLetterContactInfoPalette(
            icon = Icons.Outlined.Email,
            containerColor = colors.infoContainer,
            contentColor = colors.onInfoContainer,
        )

        CoverLetterContactInfo.NAME -> CoverLetterContactInfoPalette(
            icon = Icons.Outlined.Email,
            containerColor = colorScheme.onSurface,
            contentColor = colorScheme.surface,
        )

        else -> CoverLetterContactInfoPalette(
            icon = Icons.Outlined.Email,
            containerColor = colors.warningContainer,
            contentColor = colors.onWarningContainer,
        )
    }
}

