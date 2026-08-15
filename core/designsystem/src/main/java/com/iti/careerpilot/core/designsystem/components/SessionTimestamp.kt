package com.iti.careerpilot.core.designsystem.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import com.iti.careerpilot.core.designsystem.R
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.ChronoUnit
import java.util.Locale

@Composable
fun rememberSessionTimestamp(instant: Instant?): String {
    val locale = LocalConfiguration.current.locales[0]
    val unknownLabel = stringResource(R.string.session_timestamp_unknown)
    val todayTemplate = stringResource(R.string.session_timestamp_today)
    val yesterdayTemplate = stringResource(R.string.session_timestamp_yesterday)
    val recentTemplate = stringResource(R.string.session_timestamp_recent)

    return remember(
        instant,
        locale,
        unknownLabel,
        todayTemplate,
        yesterdayTemplate,
        recentTemplate,
    ) {
        formatSessionTimestamp(
            instant = instant,
            locale = locale,
            unknownLabel = unknownLabel,
            todayTemplate = todayTemplate,
            yesterdayTemplate = yesterdayTemplate,
            recentTemplate = recentTemplate,
        )
    }
}

internal fun formatSessionTimestamp(
    instant: Instant?,
    locale: Locale,
    unknownLabel: String,
    todayTemplate: String,
    yesterdayTemplate: String,
    recentTemplate: String,
    zoneId: ZoneId = ZoneId.systemDefault(),
    today: LocalDate = LocalDate.now(zoneId),
): String {
    if (instant == null) return unknownLabel

    val dateTime = instant.atZone(zoneId)
    val sessionDate = dateTime.toLocalDate()
    val time = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
        .withLocale(locale)
        .format(dateTime.toLocalTime())

    return when (ChronoUnit.DAYS.between(sessionDate, today)) {
        0L -> String.format(locale, todayTemplate, time)
        1L -> String.format(locale, yesterdayTemplate, time)
        in 2L until DAYS_IN_WEEK -> {
            val weekday = DateTimeFormatter.ofPattern(WEEKDAY_PATTERN, locale).format(dateTime)
            String.format(locale, recentTemplate, weekday, time)
        }
        else -> DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
            .withLocale(locale)
            .format(sessionDate)
    }
}

private const val DAYS_IN_WEEK = 7L
private const val WEEKDAY_PATTERN = "EEE"
