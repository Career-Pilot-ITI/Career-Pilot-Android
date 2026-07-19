package com.iti.onboarding.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun ScreenSubTitle(
    title: String,
    modifier: Modifier = Modifier,
    color: Color? = null,
) {

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Text(
            title,
            style = MaterialTheme.typography.bodyLarge.copy(
                color = color ?: MaterialTheme.colorScheme.onBackground
                    .copy(alpha = 0.5f)
            )
        )
    }

}