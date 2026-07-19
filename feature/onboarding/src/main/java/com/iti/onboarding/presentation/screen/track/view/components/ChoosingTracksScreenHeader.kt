package com.iti.onboarding.presentation.screen.track.view.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.iti.onboarding.R
import com.iti.onboarding.presentation.components.ScreenSubTitle
import com.iti.onboarding.presentation.components.ScreenTitle


@Composable
fun ChoosingTracksScreenHeader(modifier: Modifier = Modifier) {

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.Start
    ) {
        ScreenTitle(stringResource(R.string.choose_your_track_title))
        ScreenSubTitle(stringResource(R.string.choose_your_track_subtitle))
    }

}
