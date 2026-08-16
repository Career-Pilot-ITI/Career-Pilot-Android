package com.iti.careerpilot.core.designsystem.components

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test

class SessionTimestampTest {

    @Test
    fun `today shows the time`() {
        assertEquals("Today, 12:00 PM", format("2026-08-13T09:00:00Z"))
    }

    @Test
    fun `yesterday shows the time`() {
        assertEquals("Yesterday, 12:00 PM", format("2026-08-12T09:00:00Z"))
    }

    @Test
    fun `within the last week shows the weekday and time`() {
        assertEquals("Sun, 12:00 PM", format("2026-08-09T09:00:00Z"))
    }

    @Test
    fun `a week or more ago shows an absolute date`() {
        assertEquals("Aug 6, 2026", format("2026-08-06T09:00:00Z"))
    }

    @Test
    fun `null instant shows the unknown label`() {
        assertEquals("Date unavailable", format(null))
    }

    @Test
    fun `the instant is rendered in the device zone`() {
        val utc = format("2026-08-13T09:00:00Z", zoneId = ZoneId.of("UTC"))
        val cairo = format("2026-08-13T09:00:00Z", zoneId = ZoneId.of("Africa/Cairo"))

        assertEquals("Today, 9:00 AM", utc)
        assertEquals("Today, 12:00 PM", cairo)
    }

    /**
     * CLDR separates the time from AM/PM with a narrow no-break space, which varies by JDK
     * version. Normalise it so the assertions stay readable.
     */
    private fun format(
        timestamp: String?,
        zoneId: ZoneId = ZoneId.of("Africa/Cairo"),
    ): String = formatSessionTimestampRaw(timestamp, zoneId)
        .replace(' ', ' ')
        .replace(' ', ' ')

    private fun formatSessionTimestampRaw(
        timestamp: String?,
        zoneId: ZoneId,
    ): String = formatSessionTimestamp(
        instant = timestamp?.let(Instant::parse),
        locale = Locale.US,
        unknownLabel = "Date unavailable",
        todayTemplate = "Today, %1\$s",
        yesterdayTemplate = "Yesterday, %1\$s",
        recentTemplate = "%1\$s, %2\$s",
        zoneId = zoneId,
        today = LocalDate.of(2026, 8, 13),
    )
}
