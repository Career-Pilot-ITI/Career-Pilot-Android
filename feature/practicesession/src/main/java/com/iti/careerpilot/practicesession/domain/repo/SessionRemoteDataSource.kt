package com.iti.careerpilot.practicesession.domain.repo

import com.iti.careerpilot.practicesession.data.datasource.models.AnswerRequestDto
import com.iti.careerpilot.practicesession.data.datasource.models.AnswerResponseDto
import com.iti.careerpilot.practicesession.data.datasource.models.CreateSessionRequestDto
import com.iti.careerpilot.practicesession.data.datasource.models.FileUploadResponse
import com.iti.careerpilot.practicesession.data.datasource.models.OldSessionDto
import com.iti.careerpilot.practicesession.data.datasource.models.SessionDto
import com.iti.careerpilot.practicesession.data.datasource.models.SessionResultDto
import com.iti.common.error.NetworkError
import com.iti.common.result.CareerPilotResult
import java.io.File

interface SessionRemoteDataSource {

    suspend fun createNewSession(
        request: CreateSessionRequestDto
    ): CareerPilotResult<SessionDto, NetworkError>

    suspend fun uploadAudio(
        file: File,
        onProgress: (Int) -> Unit
    ): CareerPilotResult<FileUploadResponse, NetworkError>

    suspend fun submitAnswer(
        sessionId: Long,
        request: AnswerRequestDto
    ): CareerPilotResult<AnswerResponseDto, NetworkError>

    suspend fun getSessionFeedback(
        sessionId: Long
    ): CareerPilotResult<SessionResultDto, NetworkError>

    suspend fun getSessionState(
        sessionId: Long
    ): CareerPilotResult<OldSessionDto, NetworkError>

}