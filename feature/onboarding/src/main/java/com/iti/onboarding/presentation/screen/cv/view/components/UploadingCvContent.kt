package com.iti.onboarding.presentation.screen.cv.view.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.iti.onboarding.R
import com.iti.onboarding.presentation.screen.cv.state.SelectedCvUiModel


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun UploadingCvContent(
    selectedFile: SelectedCvUiModel?,
    progress: Float,
    isParsing: Boolean = false,
) {
    val statusText = if (isParsing || progress >= 1f) {
        stringResource(R.string.parsing_cv)
    } else {
        stringResource(
            R.string.cv_upload_progress,
            (progress * 100).toInt(),
        )
    }

    FileDetails(
        selectedFile = selectedFile,
        icon = {
            LoadingIndicator(
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp),
            )
        },
        statusText = statusText,
    )
}