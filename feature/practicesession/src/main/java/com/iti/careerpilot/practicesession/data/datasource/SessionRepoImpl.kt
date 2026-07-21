package com.iti.careerpilot.practicesession.data.datasource

import com.iti.careerpilot.practicesession.data.datasource.mapper.toDomain
import com.iti.careerpilot.practicesession.data.datasource.mapper.toDto
import com.iti.careerpilot.practicesession.domain.models.CreateSessionRequest
import com.iti.careerpilot.practicesession.domain.models.Session
import com.iti.careerpilot.practicesession.domain.repo.SessionLocalDataSource
import com.iti.careerpilot.practicesession.domain.repo.SessionRemoteDataSource
import com.iti.careerpilot.practicesession.domain.repo.SessionRepo
import com.iti.common.error.NetworkError
import com.iti.common.result.CareerPilotResult
import com.iti.common.result.map
import com.iti.common.result.onSuccess
import javax.inject.Inject

class SessionRepoImpl @Inject constructor(
    private val localDataSource: SessionLocalDataSource,
    private val remoteDataSource: SessionRemoteDataSource
): SessionRepo {


    override suspend fun createNewSession(
        request: CreateSessionRequest
    ): CareerPilotResult<Session, NetworkError> {
        return remoteDataSource.createNewSession(
            request = request.toDto()
        )
            .map {
                it.toDomain()
            }
    }


}