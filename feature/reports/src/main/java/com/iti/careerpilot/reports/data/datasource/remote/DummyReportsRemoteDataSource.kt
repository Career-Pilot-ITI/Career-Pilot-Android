package com.iti.careerpilot.reports.data.datasource.remote

import com.iti.careerpilot.reports.data.datasource.remote.dto.QuestionBreakdownDto
import com.iti.careerpilot.reports.data.datasource.remote.dto.ReportDetailsDto
import com.iti.careerpilot.reports.data.datasource.remote.dto.SessionSummaryDto
import javax.inject.Inject

class DummyReportsRemoteDataSource @Inject constructor() : ReportsRemoteDataSource {
    override suspend fun getSessionHistory(): List<SessionSummaryDto> =
        ReportsDummyData.sessions

    override suspend fun getReportDetails(
        sessionId: String,
    ): ReportDetailsDto = ReportsDummyData.detailsBySessionId[sessionId]
        ?: throw ReportsDataNotFoundException(sessionId)

    override suspend fun getQuestionBreakdown(
        sessionId: String,
    ): QuestionBreakdownDto = ReportsDummyData.breakdownsBySessionId[sessionId]
        ?: throw ReportsDataNotFoundException(sessionId)
}
