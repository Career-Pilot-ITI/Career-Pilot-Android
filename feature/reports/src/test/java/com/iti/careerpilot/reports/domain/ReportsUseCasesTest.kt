package com.iti.careerpilot.reports.domain

import com.iti.careerpilot.reports.domain.usecase.GetQuestionBreakdownUseCase
import com.iti.careerpilot.reports.domain.usecase.GetReportDetailsUseCase
import com.iti.careerpilot.reports.domain.usecase.GetSessionHistoryUseCase
import com.iti.careerpilot.reports.testutil.FakeReportsRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ReportsUseCasesTest {
    @Test
    fun `each use case delegates to its focused repository operation`() = runTest {
        val repository = FakeReportsRepository()

        GetSessionHistoryUseCase(repository)()
        GetReportDetailsUseCase(repository)("session-1")
        GetQuestionBreakdownUseCase(repository)("session-1")

        assertEquals(1, repository.historyCalls)
        assertEquals(1, repository.detailsCalls)
        assertEquals(1, repository.breakdownCalls)
    }
}
