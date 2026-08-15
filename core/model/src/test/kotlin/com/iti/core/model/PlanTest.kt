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
}
