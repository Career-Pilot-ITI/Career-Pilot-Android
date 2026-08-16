package com.iti.careerpilot.reports.data.datasource.remote

import com.iti.careerpilot.reports.data.datasource.remote.dto.FeedbackReportDto
import com.iti.careerpilot.reports.data.datasource.remote.dto.SessionQuestionDto
import com.iti.common.error.NetworkError
import com.iti.common.result.CareerPilotResult

interface ReportsRemoteDataSource {
    suspend fun getFeedback(
        sessionId: Long,
    ): CareerPilotResult<FeedbackReportDto, NetworkError>

    suspend fun getQuestions(
        sessionId: Long,
    ): CareerPilotResult<List<SessionQuestionDto>, NetworkError>
}
