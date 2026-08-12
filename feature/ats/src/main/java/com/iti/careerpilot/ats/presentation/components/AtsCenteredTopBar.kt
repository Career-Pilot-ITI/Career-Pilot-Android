package com.iti.careerpilot.ats.presentation.components

import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.iti.careerpilot.core.designsystem.components.BackIconButton

@Composable
internal fun AtsCenteredTopBar(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    CenterAlignedTopAppBar(
        title = { Text(text = title) },
        navigationIcon = { BackIconButton(onBack = onBack) },
        modifier = modifier,
    )
}
