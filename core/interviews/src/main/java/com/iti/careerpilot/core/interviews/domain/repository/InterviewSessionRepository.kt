package com.iti.careerpilot.core.interviews.domain.repository

import com.iti.careerpilot.core.interviews.domain.model.InterviewSession
import com.iti.careerpilot.core.interviews.domain.model.InterviewSessionPage
import com.iti.common.error.NetworkError
import com.iti.common.result.CareerPilotResult

interface InterviewSessionRepository {

    suspend fun getSessions(
        page: Int,
        size: Int,
    ): CareerPilotResult<InterviewSessionPage, NetworkError>

    suspend fun getSession(
        sessionId: Long,
    ): CareerPilotResult<InterviewSession, NetworkError>
}
