package com.iti.onboarding.presentation.screen.cv.view.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.iti.onboarding.presentation.screen.cv.state.SelectedCvUiModel


@Composable
fun SelectedCvContent(
    selectedFile: SelectedCvUiModel?,
    statusText: String,
) {
    FileDetails(
        selectedFile = selectedFile,
        icon = {
            Icon(
                imageVector = Icons.Outlined.FileUpload,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(42.dp),
            )
        },
        statusText = statusText,
    )
}