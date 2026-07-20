package com.iti.careerpilot.reports.data.datasource.remote

import com.iti.careerpilot.reports.data.datasource.remote.dto.QuestionBreakdownDto
import com.iti.careerpilot.reports.data.datasource.remote.dto.ReportDetailsDto
import com.iti.careerpilot.reports.data.datasource.remote.dto.SessionSummaryDto

interface ReportsRemoteDataSource {
    suspend fun getSessionHistory(): List<SessionSummaryDto>

    suspend fun getReportDetails(
        sessionId: String,
    ): ReportDetailsDto

    suspend fun getQuestionBreakdown(
        sessionId: String,
    ): QuestionBreakdownDto
}
