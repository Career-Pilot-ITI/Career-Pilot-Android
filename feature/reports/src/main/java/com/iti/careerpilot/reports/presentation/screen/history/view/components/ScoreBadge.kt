package com.iti.careerpilot.reports.presentation.screen.history.view.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.iti.careerpilot.core.designsystem.Dimens
import com.iti.careerpilot.reports.presentation.screen.history.util.scoreColor

@Composable
fun ScoreBadge(
    score: Int,
    modifier: Modifier = Modifier,
) {
    val foreground = scoreColor(score)
    Surface(
        modifier = modifier.size(Dimens.ScoreBadgeSize),
        shape = RoundedCornerShape(Dimens.SpaceM),
        color = foreground.copy(alpha = 0.10f),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = score.toString(),
                style = MaterialTheme.typography.titleMedium,
                color = foreground,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}