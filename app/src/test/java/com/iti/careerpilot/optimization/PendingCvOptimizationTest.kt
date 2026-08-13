package com.iti.careerpilot.optimization

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PendingCvOptimizationTest {
    @Test
    fun `valid notification deep link extracts workspace and job ids`() {
        assertEquals(
            PendingCvOptimization(workspaceId = 7L, jobId = 42L),
            PendingCvOptimization.from("careerpilot://ats/optimized-cv/7/42"),
        )
    }

    @Test
    fun `malformed notification deep link is ignored`() {
        assertNull(PendingCvOptimization.from("careerpilot://ats/optimized-cv/not-an-id/42"))
    }
}
