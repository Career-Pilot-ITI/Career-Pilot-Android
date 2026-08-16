package com.iti.common.util

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateTimeUtil {

    fun formatSubscriptionDate(rawDate: String?, locale: Locale = Locale.getDefault()): String? {
        if (rawDate.isNullOrBlank()) return null
        val outputFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy", locale)
        return runCatching {
            when {
                rawDate.toLongOrNull() != null -> {
                    val instant = Instant.ofEpochMilli(rawDate.toLong())
                    val zdt = instant.atZone(ZoneId.systemDefault())
                    zdt.format(outputFormatter)
                }
                rawDate.contains("T") -> {
                    try {
                        val zdt = ZonedDateTime.parse(rawDate, DateTimeFormatter.ISO_DATE_TIME)
                        zdt.format(outputFormatter)
                    } catch (_: Throwable) {
                        val ldt = LocalDateTime.parse(rawDate, DateTimeFormatter.ISO_DATE_TIME)
                        ldt.format(outputFormatter)
                    }
                }
                rawDate.contains("-") -> {
                    val ld = LocalDate.parse(rawDate, DateTimeFormatter.ISO_LOCAL_DATE)
                    ld.format(outputFormatter)
                }
                else -> rawDate
            }
        }.getOrDefault(rawDate)
    }
}
