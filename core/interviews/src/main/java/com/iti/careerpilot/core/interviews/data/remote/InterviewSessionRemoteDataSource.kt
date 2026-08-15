package com.iti.careerpilot.core.interviews.data.remote

import com.iti.careerpilot.core.interviews.data.remote.dto.InterviewSessionDto
import com.iti.careerpilot.core.interviews.data.remote.dto.InterviewSessionPageDto
import com.iti.common.error.NetworkError
import com.iti.common.result.CareerPilotResult

interface InterviewSessionRemoteDataSource {

    suspend fun getSessions(
        page: Int,
        size: Int,
    ): CareerPilotResult<InterviewSessionPageDto, NetworkError>

    suspend fun getSession(
        sessionId: Long,
    ): CareerPilotResult<InterviewSessionDto, NetworkError>
}
