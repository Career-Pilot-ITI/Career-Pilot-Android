package com.iti.onboarding.presentation.screen.cv.view.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.iti.onboarding.R
import com.iti.onboarding.presentation.components.ScreenSubTitle
import com.iti.onboarding.presentation.components.ScreenTitle

@Composable
fun UploadCvHeader(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ScreenTitle(
            title = stringResource(R.string.upload_cv_title),
        )
        ScreenSubTitle(
            title = stringResource(R.string.upload_cv_subtitle),
        )
    }
}
