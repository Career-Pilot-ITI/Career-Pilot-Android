package com.iti.careerpilot.practicesession.data.datasource.remote

import com.iti.careerpilot.practicesession.data.datasource.models.CreateSessionRequestDto
import com.iti.careerpilot.practicesession.data.datasource.models.SessionDto
import com.iti.careerpilot.practicesession.domain.repo.SessionRemoteDataSource
import com.iti.common.error.NetworkError
import com.iti.common.result.CareerPilotResult
import io.ktor.client.HttpClient
import javax.inject.Inject

class SessionRemoteDataSourceImpl @Inject constructor(
    private val httpClient: HttpClient
): SessionRemoteDataSource {


    override suspend fun createNewSession(
        request: CreateSessionRequestDto
    ): CareerPilotResult<SessionDto, NetworkError> {
        TODO(" Not yet implemented use httpClient")
    }


}