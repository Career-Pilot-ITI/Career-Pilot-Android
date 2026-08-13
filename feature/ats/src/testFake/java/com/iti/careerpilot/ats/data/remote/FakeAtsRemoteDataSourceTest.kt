package com.iti.careerpilot.ats.data.remote

import com.iti.common.error.NetworkError
import com.iti.common.result.CareerPilotResult
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FakeAtsRemoteDataSourceTest {
    private val source = FakeAtsRemoteDataSource()

    @Test
    fun `fake source exposes all documented v1 operations for one workspace`() = runTest {
        val imported = source.importJob("https://example.com/job")
        val workspaceId = (imported as CareerPilotResult.Success).data.id

        assertTrue(source.getWorkspace(workspaceId) is CareerPilotResult.Success)
        assertTrue(source.scoreCv(workspaceId) is CareerPilotResult.Success)
        val optimization = source.optimizeCv(workspaceId) as CareerPilotResult.Success
        assertTrue(source.getAiJob(optimization.data.id) is CareerPilotResult.Success)
        assertTrue(source.generateCoverLetter(workspaceId) is CareerPilotResult.Success)
    }

    @Test
    fun `fake source returns not found for an unknown workspace`() = runTest {
        val result = source.getWorkspace(Long.MAX_VALUE)

        assertEquals(NetworkError.NOT_FOUND, (result as CareerPilotResult.Error).error)
    }
}
