package com.iti.onboarding.presentation.screen.track.view.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.iti.onboarding.domain.model.Track


@Composable
fun TrackChip(
    track: Track,
    onClick: () -> Unit,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
) {
    val selectedContainerColor = MaterialTheme.colorScheme.onBackground
    val selectedContentColor = MaterialTheme.colorScheme.onPrimary

    val unselectedContainerColor = MaterialTheme.colorScheme.background
    val unselectedContentColor = MaterialTheme.colorScheme.onBackground
    val unselectedBorderColor = MaterialTheme.colorScheme.onBackground.copy(
        alpha = 0.3f
    )

    FilterChip(
        selected = isSelected,
        onClick = onClick,
        modifier = modifier.height(46.dp),
        label = {
            Text(
                text = track.name,
                modifier = Modifier.padding(horizontal = 4.dp),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        shape = RoundedCornerShape(percent = 50),
        colors = FilterChipDefaults.filterChipColors(
            containerColor = unselectedContainerColor,
            labelColor = unselectedContentColor,
            selectedContainerColor = selectedContainerColor,
            selectedLabelColor = selectedContentColor,
        ),
        border = if (isSelected) {
            null
        } else {
            BorderStroke(
                width = 1.dp,
                color = unselectedBorderColor,
            )
        },
    )
}