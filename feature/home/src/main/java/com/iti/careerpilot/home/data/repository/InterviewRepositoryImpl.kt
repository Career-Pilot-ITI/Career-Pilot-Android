package com.iti.careerpilot.home.data.repository

import com.iti.careerpilot.home.data.mapper.toDomain
import com.iti.careerpilot.home.data.datasource.remote.InterviewRemoteDataSource
import com.iti.careerpilot.home.domain.model.InterviewSession
import com.iti.careerpilot.home.domain.model.InterviewTrack
import com.iti.careerpilot.home.domain.repository.InterviewRepository
import com.iti.common.error.NetworkError
import com.iti.common.result.CareerPilotResult
import javax.inject.Inject

class InterviewRepositoryImpl @Inject constructor(
    private val remoteDataSource: InterviewRemoteDataSource,
) : InterviewRepository {

    override suspend fun getInterviewSessions(): CareerPilotResult<List<InterviewSession>, NetworkError> =
        when (val result = remoteDataSource.getInterviewSessions()) {
            is CareerPilotResult.Error -> result
            is CareerPilotResult.Success -> CareerPilotResult.Success(
                result.data.toDomain().sortedByDescending { it.occurredAt }
            )
        }

    override suspend fun getTracks(): CareerPilotResult<List<InterviewTrack>, NetworkError> =
        when (val result = remoteDataSource.getTracks()) {
            is CareerPilotResult.Error -> result
            is CareerPilotResult.Success -> CareerPilotResult.Success(result.data.toDomain())
        }
}
