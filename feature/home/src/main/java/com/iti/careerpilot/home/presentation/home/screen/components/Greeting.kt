package com.iti.careerpilot.home.presentation.home.screen.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.iti.careerpilot.home.R
import java.time.LocalTime

@Composable
fun rememberGreeting(): String {
    val hour = LocalTime.now().hour

    return stringResource(
        when (hour) {
            in MORNING_START until AFTERNOON_START -> R.string.home_greeting_morning
            in AFTERNOON_START until EVENING_START -> R.string.home_greeting_afternoon
            else -> R.string.home_greeting_evening
        }
    )
}

private const val MORNING_START = 5
private const val AFTERNOON_START = 12
private const val EVENING_START = 18
