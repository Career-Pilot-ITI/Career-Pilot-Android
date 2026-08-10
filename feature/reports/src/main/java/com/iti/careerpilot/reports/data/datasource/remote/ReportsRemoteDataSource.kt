package com.iti.careerpilot.reports.data.datasource.remote

import com.iti.careerpilot.reports.data.datasource.remote.dto.FeedbackReportDto
import com.iti.careerpilot.reports.data.datasource.remote.dto.InterviewSessionDto
import com.iti.careerpilot.reports.data.datasource.remote.dto.InterviewSessionsPageDto
import com.iti.careerpilot.reports.data.datasource.remote.dto.SessionQuestionDto
import com.iti.common.error.NetworkError
import com.iti.common.result.CareerPilotResult

interface ReportsRemoteDataSource {
    suspend fun getSessions(
        page: Int,
        size: Int,
    ): CareerPilotResult<InterviewSessionsPageDto, NetworkError>

    suspend fun getSession(
        sessionId: Long,
    ): CareerPilotResult<InterviewSessionDto, NetworkError>

    suspend fun getFeedback(
        sessionId: Long,
    ): CareerPilotResult<FeedbackReportDto, NetworkError>

    suspend fun getQuestions(
        sessionId: Long,
    ): CareerPilotResult<List<SessionQuestionDto>, NetworkError>
}
