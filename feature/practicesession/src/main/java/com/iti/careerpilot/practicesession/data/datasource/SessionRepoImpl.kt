package com.iti.careerpilot.practicesession.data.datasource

import com.iti.careerpilot.practicesession.data.datasource.mapper.toDomain
import com.iti.careerpilot.practicesession.data.datasource.mapper.toDto
import com.iti.careerpilot.practicesession.domain.models.AnswerRequest
import com.iti.careerpilot.practicesession.domain.models.AnswerResponse
import com.iti.careerpilot.practicesession.domain.models.AudioAttachment
import com.iti.careerpilot.practicesession.domain.models.CreateSessionRequest
import com.iti.careerpilot.practicesession.domain.models.Session
import com.iti.careerpilot.practicesession.domain.models.SessionResult
import com.iti.careerpilot.practicesession.domain.repo.SessionRemoteDataSource
import com.iti.careerpilot.practicesession.domain.repo.SessionRepo
import com.iti.common.error.NetworkError
import com.iti.common.result.CareerPilotResult
import com.iti.common.result.map
import java.io.File
import javax.inject.Inject

class SessionRepoImpl @Inject constructor(
    private val remoteDataSource: SessionRemoteDataSource
) : SessionRepo {

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

    override suspend fun uploadAudio(
        file: File,
        onProgress: (Int) -> Unit
    ): CareerPilotResult<AudioAttachment, NetworkError> {
        return remoteDataSource.uploadAudio(
            file = file,
            onProgress = onProgress
        )
            .map {
                it.toDomain()
            }
    }

    override suspend fun submitAnswer(
        sessionId: Int,
        request: AnswerRequest
    ): CareerPilotResult<AnswerResponse, NetworkError> {
        return remoteDataSource.submitAnswer(
            sessionId = sessionId,
            request = request.toDto()
        )
            .map {
                it.toDomain()
            }
    }

    override suspend fun getSessionFeedback(
        sessionId: Int
    ): CareerPilotResult<SessionResult, NetworkError> {
        return remoteDataSource.getSessionFeedback(sessionId)
            .map {
                it.toDomain()
            }
    }

    override suspend fun getSessionState(
        sessionId: Int
    ): CareerPilotResult<Session, NetworkError> {
        return remoteDataSource.getSessionState(sessionId)
            .map {
                it.toDomain()
            }
    }

}