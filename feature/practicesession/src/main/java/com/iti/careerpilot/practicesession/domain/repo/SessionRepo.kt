package com.iti.careerpilot.practicesession.domain.repo

import com.iti.careerpilot.practicesession.domain.models.AnswerRequest
import com.iti.careerpilot.practicesession.domain.models.AnswerResponse
import com.iti.careerpilot.practicesession.domain.models.AudioAttachment
import com.iti.careerpilot.practicesession.domain.models.CreateSessionRequest
import com.iti.careerpilot.practicesession.domain.models.Session
import com.iti.careerpilot.practicesession.domain.models.SessionResult
import com.iti.common.error.NetworkError
import com.iti.common.result.CareerPilotResult
import java.io.File

interface SessionRepo {

    suspend fun createNewSession(
        request: CreateSessionRequest
    ): CareerPilotResult<Session, NetworkError>

    suspend fun uploadAudio(
        file: File,
        onProgress: (Int) -> Unit
    ): CareerPilotResult<AudioAttachment, NetworkError>

    suspend fun submitAnswer(
        sessionId: Long,
        request: AnswerRequest
    ): CareerPilotResult<AnswerResponse, NetworkError>

    suspend fun getSessionFeedback(
        sessionId: Long
    ): CareerPilotResult<SessionResult, NetworkError>

    suspend fun restartOldSession(
        sessionId: Long
    ): CareerPilotResult<Session, NetworkError>

}
