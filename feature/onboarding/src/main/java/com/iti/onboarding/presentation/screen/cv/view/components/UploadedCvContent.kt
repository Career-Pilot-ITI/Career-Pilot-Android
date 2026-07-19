package com.iti.onboarding.presentation.screen.cv.view.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.iti.onboarding.R
import com.iti.onboarding.presentation.screen.cv.state.SelectedCvUiModel

@Composable
fun UploadedCvContent(
    selectedFile: SelectedCvUiModel?,
) {
    FileDetails(
        selectedFile = selectedFile,
        icon = {
            Icon(
                imageVector = Icons.Outlined.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(48.dp),
            )
        },
        statusText = stringResource(R.string.cv_uploaded_label),
    )
}