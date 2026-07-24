package com.iti.careerpilot.reports.domain.repository

import com.iti.careerpilot.reports.domain.model.InterviewSessionSummary
import com.iti.careerpilot.reports.domain.model.QuestionBreakdown
import com.iti.careerpilot.reports.domain.model.ReportDetails
import com.iti.common.error.NetworkError
import com.iti.common.result.CareerPilotResult

interface ReportsRepository {
    suspend fun getSessionHistory(): CareerPilotResult<List<InterviewSessionSummary>, NetworkError>

    suspend fun getReportDetails(
        sessionId: Long,
    ): CareerPilotResult<ReportDetails, NetworkError>

    suspend fun getQuestionBreakdown(
        sessionId: Long,
    ): CareerPilotResult<QuestionBreakdown, NetworkError>
}
