package com.iti.careerpilot.practicesession.presentation.practicescreen.screen.util

fun formatDuration(millis: Long): String {
    val totalSeconds = (millis / 1000).coerceAtLeast(0)
    val seconds = totalSeconds % 60
    val minutes = (totalSeconds / 60) % 60
    return "%02d:%02d".format(minutes, seconds)
}
