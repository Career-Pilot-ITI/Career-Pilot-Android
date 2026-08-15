package com.iti.common.util

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateTimeUtil {
    private val outputFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.US)

    fun formatSubscriptionDate(rawDate: String?): String? {
        if (rawDate.isNullOrBlank()) return null
        return runCatching {
            when {
                rawDate.contains("T") -> {
                    try {
                        val zdt = ZonedDateTime.parse(rawDate, DateTimeFormatter.ISO_DATE_TIME)
                        zdt.format(outputFormatter)
                    } catch (e: Exception) {
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
