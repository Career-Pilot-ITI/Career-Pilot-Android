package com.iti.common.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DateTimeUtilTest {

    @Test
    fun `formatSubscriptionDate formats ISO 8601 timestamp properly`() {
        val formatted = DateTimeUtil.formatSubscriptionDate("2026-08-15T12:00:00Z", java.util.Locale.US)
        assertEquals("Aug 15, 2026", formatted)
    }

    @Test
    fun `formatSubscriptionDate formats ISO 8601 with millis properly`() {
        val formatted = DateTimeUtil.formatSubscriptionDate("2026-08-15T12:00:00.000Z", java.util.Locale.US)
        assertEquals("Aug 15, 2026", formatted)
    }

    @Test
    fun `formatSubscriptionDate formats local date time without timezone properly`() {
        val formatted = DateTimeUtil.formatSubscriptionDate("2026-08-15T12:00:00", java.util.Locale.US)
        assertEquals("Aug 15, 2026", formatted)
    }

    @Test
    fun `formatSubscriptionDate formats YYYY-MM-DD date properly`() {
        val formatted = DateTimeUtil.formatSubscriptionDate("2026-12-31", java.util.Locale.US)
        assertEquals("Dec 31, 2026", formatted)
    }

    @Test
    fun `formatSubscriptionDate returns null for null or blank input`() {
        assertNull(DateTimeUtil.formatSubscriptionDate(null))
        assertNull(DateTimeUtil.formatSubscriptionDate(""))
        assertNull(DateTimeUtil.formatSubscriptionDate("   "))
    }

    @Test
    fun `formatSubscriptionDate formats numeric epoch timestamp properly`() {
        // 1786713600000L is approx Aug 15, 2026 UTC
        val formatted = DateTimeUtil.formatSubscriptionDate("1786713600000", java.util.Locale.US)
        org.junit.Assert.assertNotNull(formatted)
        org.junit.Assert.assertTrue(formatted!!.contains("2026"))
    }

    @Test
    fun `formatSubscriptionDate returns original string gracefully on unparseable format`() {
        val raw = "Next month"
        assertEquals("Next month", DateTimeUtil.formatSubscriptionDate(raw))
    }
}
