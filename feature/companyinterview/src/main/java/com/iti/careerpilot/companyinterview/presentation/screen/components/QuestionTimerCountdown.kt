package com.iti.careerpilot.companyinterview.presentation.screen.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun QuestionTimerCountdown(
    remainingSeconds: Int,
    totalSeconds: Int,
    modifier: Modifier = Modifier
) {
    val progress = if (totalSeconds > 0) remainingSeconds.toFloat() / totalSeconds.toFloat() else 0f
    val isUrgent = remainingSeconds <= 10

    val barColor by animateColorAsState(
        targetValue = if (isUrgent) Color(0xFFDC2626) else MaterialTheme.colorScheme.primary,
        label = "timerColor"
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Timer,
                contentDescription = "Timer",
                tint = barColor,
                modifier = Modifier.size(18.dp)
            )
            val minutes = remainingSeconds / 60
            val seconds = remainingSeconds % 60
            Text(
                text = String.format("%02d:%02d", minutes, seconds),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 22.sp
                ),
                color = barColor
            )
        }

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape),
            color = barColor,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}
