package com.iti.careerpilot.practicesession.data.datasource.remote

import com.iti.careerpilot.core.network.Endpoints
import com.iti.careerpilot.core.network.util.safeCall
import com.iti.careerpilot.practicesession.data.datasource.models.AnswerRequestDto
import com.iti.careerpilot.practicesession.data.datasource.models.AnswerResponseDto
import com.iti.careerpilot.practicesession.data.datasource.models.CreateSessionRequestDto
import com.iti.careerpilot.practicesession.data.datasource.models.FileUploadResponse
import com.iti.careerpilot.practicesession.data.datasource.models.OldSessionDto
import com.iti.careerpilot.practicesession.data.datasource.models.SessionDto
import com.iti.careerpilot.practicesession.data.datasource.models.SessionResultDto
import com.iti.careerpilot.practicesession.domain.repo.SessionRemoteDataSource
import com.iti.common.error.NetworkError
import com.iti.common.result.CareerPilotResult
import io.ktor.client.HttpClient
import io.ktor.client.plugins.onUpload
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import java.io.File
import javax.inject.Inject

class SessionRemoteDataSourceImpl @Inject constructor(
    private val httpClient: HttpClient
): SessionRemoteDataSource {

    override suspend fun createNewSession(
        request: CreateSessionRequestDto
    ): CareerPilotResult<SessionDto, NetworkError> {
        return safeCall {
            httpClient.post(Endpoints.INTERVIEW_SESSIONS) {
                setBody(request)
            }
        }
    }

    override suspend fun uploadAudio(
        file: File,
        onProgress: (Int) -> Unit
    ): CareerPilotResult<FileUploadResponse, NetworkError> {
        return safeCall {
            httpClient.submitFormWithBinaryData(
                url = Endpoints.UPLOAD_FILE,
                formData = formData {
                    append(
                        key = "file",
                        value = file.readBytes(),
                        headers = Headers.build {
                            append(HttpHeaders.ContentType, "audio/mpeg")
                            append(HttpHeaders.ContentDisposition, "filename=\"${file.name}\"")
                        }
                    )
                    append("type", "audios")
                }
            ) {
                onUpload { bytesSentTotal, contentLength ->
                    if (contentLength != null && contentLength > 0) {
                        val percent = ((bytesSentTotal * 100) / contentLength).toInt().coerceIn(0, 100)
                        onProgress(percent)
                    }
                }
            }
        }
    }

    /**
     * When this returns "sessionStatus": "READY_TO_COMPLETE" then call
     * [SessionRemoteDataSource.getSessionFeedback]
     */
    override suspend fun submitAnswer(
        sessionId: Long,
        request: AnswerRequestDto
    ): CareerPilotResult<AnswerResponseDto, NetworkError> {
        return safeCall {
            httpClient.post(Endpoints.SUBMIT_ANSWER(sessionId)) {
                setBody(request)
            }
        }
    }

    override suspend fun getSessionFeedback(
        sessionId: Long
    ): CareerPilotResult<SessionResultDto, NetworkError> {
        return safeCall {
            httpClient.get(Endpoints.GET_SESSION_FEEDBACK(sessionId))
        }
    }

    override suspend fun getSessionState(
        sessionId: Long
    ): CareerPilotResult<OldSessionDto, NetworkError> {
        return safeCall {
            httpClient.get(Endpoints.GET_SESSION_STATE(sessionId))
        }
    }


}