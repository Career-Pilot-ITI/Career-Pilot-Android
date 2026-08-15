package com.iti.careerpilot.ats.presentation.coverletter.view.components

import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.iti.careerpilot.ats.presentation.coverletter.uimodels.CoverLetterContactInfoPalette

@Composable
internal fun CoverLetterContactInfoCard(
    palette: CoverLetterContactInfoPalette,
    info: String
) {
    AssistChip(
        onClick = {},
        border = null,
        colors = AssistChipDefaults.assistChipColors(
            containerColor = palette.containerColor,
            labelColor = palette.contentColor,
            leadingIconContentColor = palette.contentColor,
        ),
        leadingIcon = {
            Icon(
                imageVector = palette.icon,
                contentDescription = null
            )
        },
        label = { Text(info) },
    )
}
