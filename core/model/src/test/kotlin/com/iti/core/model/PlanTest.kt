package com.iti.core.model

import org.junit.Assert.assertEquals
import org.junit.Test

class PlanTest {

    @Test
    fun `displayName returns correct string for all plans`() {
        assertEquals("Free", Plan.FREE.displayName())
        assertEquals("Plus", Plan.PLUS.displayName())
        assertEquals("Max", Plan.MAX.displayName())
    }

    @Test
    fun `fromString maps uppercase PLUS to PLUS`() {
        assertEquals(Plan.PLUS, Plan.fromString("PLUS"))
    }

    @Test
    fun `fromString maps lowercase plus to PLUS`() {
        assertEquals(Plan.PLUS, Plan.fromString("plus"))
    }

    @Test
    fun `fromString maps PRO to MAX`() {
        assertEquals(Plan.MAX, Plan.fromString("PRO"))
    }

    @Test
    fun `fromString maps MAX to MAX`() {
        assertEquals(Plan.MAX, Plan.fromString("MAX"))
    }

    @Test
    fun `fromString maps FREE to FREE`() {
        assertEquals(Plan.FREE, Plan.fromString("FREE"))
    }

    @Test
    fun `fromString maps UNKNOWN to FREE`() {
        assertEquals(Plan.FREE, Plan.fromString("UNKNOWN"))
    }

    @Test
    fun `fromString maps empty string to FREE`() {
        assertEquals(Plan.FREE, Plan.fromString(""))
    }

    @Test
    fun `fromString handles whitespace and maps to MAX`() {
        assertEquals(Plan.MAX, Plan.fromString("  MAX  "))
    }
}
