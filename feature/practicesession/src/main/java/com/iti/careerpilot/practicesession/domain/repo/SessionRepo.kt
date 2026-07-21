package com.iti.careerpilot.practicesession.domain.repo

import com.iti.careerpilot.practicesession.domain.models.CreateSessionRequest
import com.iti.careerpilot.practicesession.domain.models.Session
import com.iti.common.error.NetworkError
import com.iti.common.result.CareerPilotResult

interface SessionRepo {

    suspend fun createNewSession(
        request: CreateSessionRequest
    ): CareerPilotResult<Session, NetworkError>

}