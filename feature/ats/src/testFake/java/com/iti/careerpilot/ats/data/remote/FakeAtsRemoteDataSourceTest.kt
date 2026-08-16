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
    fun `fake source completes the ATS workflow`() = runTest {
        val imported = source.importJob("https://www.linkedin.com/jobs/view/123456789")
        val workspaceId = (imported as CareerPilotResult.Success).data.id

        val workspace = source.getWorkspace(workspaceId) as CareerPilotResult.Success
        assertEquals("Senior Frontend Engineer", workspace.data.job.title)

        val score = source.scoreCv(workspaceId) as CareerPilotResult.Success
        assertTrue(score.data.sections.isNotEmpty())
        assertTrue(score.data.recommendations.isNotEmpty())

        val optimization = source.optimizeCv(workspaceId) as CareerPilotResult.Success
        assertEquals("PENDING", optimization.data.status)
        val completedJob = source.getAiJob(optimization.data.id) as CareerPilotResult.Success
        assertEquals("COMPLETED", completedJob.data.status)
        val result = completedJob.data.result ?: error("Expected fake CV optimization result")
        assertTrue(result.sections.first().improvements.isNotEmpty())
        assertTrue(result.sections.last().improvements.isEmpty())

        val coverLetter = source.generateCoverLetter(workspaceId) as CareerPilotResult.Success
        assertTrue(coverLetter.data.coverLetter.orEmpty().isNotBlank())
        assertTrue(coverLetter.data.approachTips.orEmpty().contains("2."))
    }

    @Test
    fun `fake source returns not found for an unknown workspace`() = runTest {
        val result = source.getWorkspace(Long.MAX_VALUE)

        assertEquals(NetworkError.NOT_FOUND, (result as CareerPilotResult.Error).error)
    }
}
