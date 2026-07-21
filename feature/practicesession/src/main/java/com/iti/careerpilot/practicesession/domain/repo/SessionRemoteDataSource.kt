package com.iti.careerpilot.practicesession.domain.repo

import com.iti.careerpilot.practicesession.data.datasource.models.CreateSessionRequestDto
import com.iti.careerpilot.practicesession.data.datasource.models.SessionDto
import com.iti.common.error.NetworkError
import com.iti.common.result.CareerPilotResult

interface SessionRemoteDataSource {

    suspend fun createNewSession(
        request: CreateSessionRequestDto
    ): CareerPilotResult<SessionDto, NetworkError>

}