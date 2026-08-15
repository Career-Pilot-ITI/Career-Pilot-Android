package com.iti.careerpilot.share

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PendingSharedTextTest {
    @Test
    fun `empty shared text is ignored`() {
        assertNull(PendingSharedText.from("   "))
    }

    @Test
    fun `raw shared input is bounded before entering app state`() {
        val result = PendingSharedText.from("x".repeat(PendingSharedText.MAX_CAPTURED_LENGTH + 100))

        assertEquals(PendingSharedText.MAX_CAPTURED_LENGTH, result?.value?.length)
    }
}
