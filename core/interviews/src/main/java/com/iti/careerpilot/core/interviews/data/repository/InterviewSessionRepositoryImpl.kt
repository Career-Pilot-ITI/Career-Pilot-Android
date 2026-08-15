package com.iti.careerpilot.core.interviews.data.repository

import com.iti.careerpilot.core.interviews.data.mapper.toDomain
import com.iti.careerpilot.core.interviews.data.remote.InterviewSessionRemoteDataSource
import com.iti.careerpilot.core.interviews.domain.model.InterviewSession
import com.iti.careerpilot.core.interviews.domain.model.InterviewSessionPage
import com.iti.careerpilot.core.interviews.domain.repository.InterviewSessionRepository
import com.iti.common.error.NetworkError
import com.iti.common.result.CareerPilotResult
import javax.inject.Inject

class InterviewSessionRepositoryImpl @Inject constructor(
    private val remoteDataSource: InterviewSessionRemoteDataSource,
) : InterviewSessionRepository {

    override suspend fun getSessions(
        page: Int,
        size: Int,
    ): CareerPilotResult<InterviewSessionPage, NetworkError> =
        when (val result = remoteDataSource.getSessions(page = page, size = size)) {
            is CareerPilotResult.Error -> result
            is CareerPilotResult.Success -> CareerPilotResult.Success(result.data.toDomain())
        }

    override suspend fun getSession(
        sessionId: Long,
    ): CareerPilotResult<InterviewSession, NetworkError> =
        when (val result = remoteDataSource.getSession(sessionId)) {
            is CareerPilotResult.Error -> result
            is CareerPilotResult.Success -> result.data.toDomain()
                ?.let { CareerPilotResult.Success(it) }
                ?: CareerPilotResult.Error(NetworkError.EMPTY_RESULT)
        }
}
