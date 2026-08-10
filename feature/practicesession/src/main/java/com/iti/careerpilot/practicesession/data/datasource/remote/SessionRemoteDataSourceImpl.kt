package com.iti.careerpilot.practicesession.data.datasource.remote

import com.iti.careerpilot.core.network.Endpoints
import com.iti.careerpilot.practicesession.data.datasource.models.CareerPilotApiResponse
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
import com.iti.common.result.map
import io.ktor.client.HttpClient
import io.ktor.client.plugins.onUpload
import io.ktor.client.plugins.timeout
import io.ktor.client.request.forms.InputProvider
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.utils.io.streams.asInput
import kotlinx.io.buffered
import java.io.File
import javax.inject.Inject

class SessionRemoteDataSourceImpl @Inject constructor(
    private val httpClient: HttpClient
): SessionRemoteDataSource {

    override suspend fun createNewSession(
        request: CreateSessionRequestDto
    ): CareerPilotResult<SessionDto, NetworkError> {
        return safeCall<CareerPilotApiResponse<SessionDto>> {
            httpClient.post(Endpoints.INTERVIEW_SESSIONS) {
                timeout {
                    requestTimeoutMillis = AI_REQUEST_TIMEOUT_MILLIS
                    socketTimeoutMillis = AI_REQUEST_TIMEOUT_MILLIS
                }
                setBody(request)
            }
        }.map { it.data }
    }

    override suspend fun uploadAudio(
        file: File,
        onProgress: (Int) -> Unit
    ): CareerPilotResult<FileUploadResponse, NetworkError> {
        val contentType = when (file.extension.lowercase()) {
            "mp3" -> "audio/mpeg"
            "wav" -> "audio/wav"
            "ogg" -> "audio/ogg"
            "m4a" -> "audio/mp4"
            "mp4" -> "audio/mp4"
            else -> "audio/mpeg"
        }
        return safeCall<FileUploadResponse> {
            httpClient.post(Endpoints.UPLOAD_FILE) {
                timeout {
                    requestTimeoutMillis = AI_REQUEST_TIMEOUT_MILLIS
                    socketTimeoutMillis = AI_REQUEST_TIMEOUT_MILLIS
                }
                setBody(
                    MultiPartFormDataContent(
                        formData {
                            append("type", "audios")
                            append(
                                key = "file",
                                value = InputProvider(size = file.length()) {
                                    file.inputStream().asInput().buffered()
                                },
                                headers = Headers.build {
                                    append(HttpHeaders.ContentType, contentType)
                                    append(HttpHeaders.ContentDisposition, "filename=\"${file.name}\"")
                                }
                            )
                        }
                    )
                )
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
        return safeCall<CareerPilotApiResponse<AnswerResponseDto>> {
            httpClient.post(Endpoints.SUBMIT_ANSWER(sessionId)) {
                timeout {
                    requestTimeoutMillis = AI_REQUEST_TIMEOUT_MILLIS
                    socketTimeoutMillis = AI_REQUEST_TIMEOUT_MILLIS
                }
                setBody(request)
            }
        }.map { it.data }
    }

    override suspend fun getSessionFeedback(
        sessionId: Long
    ): CareerPilotResult<SessionResultDto, NetworkError> {
        return safeCall<CareerPilotApiResponse<SessionResultDto>> {
            httpClient.get(Endpoints.GET_SESSION_FEEDBACK(sessionId)) {
                timeout {
                    requestTimeoutMillis = AI_REQUEST_TIMEOUT_MILLIS
                    socketTimeoutMillis = AI_REQUEST_TIMEOUT_MILLIS
                }
            }
        }.map { it.data }
    }

    override suspend fun getSessionState(
        sessionId: Long
    ): CareerPilotResult<OldSessionDto, NetworkError> {
        return safeCall<CareerPilotApiResponse<OldSessionDto>> {
            httpClient.get(Endpoints.GET_SESSION_STATE(sessionId))
        }.map { it.data }
    }

    companion object {
        private const val AI_REQUEST_TIMEOUT_MILLIS = 120_000L
    }
}