package com.iti.careerpilot.practicesession.data.datasource.mapper

import com.iti.careerpilot.practicesession.domain.models.CreateSessionRequest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SessionMappersTest {

    @Test
    fun `create session maps ATS workspace id`() {
        val dto = CreateSessionRequest(
            trackId = 5L,
            questionCount = 10,
            durationMinutes = 20,
            workspaceId = 42L,
        ).toDto()

        assertEquals(42L, dto.workspaceId)
    }

    @Test
    fun `create session keeps workspace optional for standard practice`() {
        val dto = CreateSessionRequest(
            trackId = 5L,
            questionCount = 10,
            durationMinutes = 20,
        ).toDto()

        assertNull(dto.workspaceId)
    }
}
