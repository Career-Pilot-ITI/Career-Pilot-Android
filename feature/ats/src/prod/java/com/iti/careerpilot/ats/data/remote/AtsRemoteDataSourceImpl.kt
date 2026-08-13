package com.iti.careerpilot.ats.data.remote

import com.iti.careerpilot.ats.data.dto.AtsApiResponseDto
import com.iti.careerpilot.ats.data.dto.AiJobDto
import com.iti.careerpilot.ats.data.dto.AtsScoreDto
import com.iti.careerpilot.ats.data.dto.CoverLetterDto
import com.iti.careerpilot.ats.data.dto.ImportJobRequestDto
import com.iti.careerpilot.ats.data.dto.JobWorkspaceDto
import com.iti.careerpilot.core.network.Endpoints
import com.iti.careerpilot.core.network.model.UserProfileDto
import com.iti.careerpilot.core.network.util.safeCall
import com.iti.common.error.NetworkError
import com.iti.common.result.CareerPilotResult
import com.iti.core.model.PdfFile
import io.ktor.client.HttpClient
import io.ktor.client.plugins.onUpload
import io.ktor.client.plugins.timeout
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import javax.inject.Inject

class AtsRemoteDataSourceImpl @Inject constructor(
    private val client: HttpClient,
) : AtsRemoteDataSource {
    override suspend fun replaceCurrentCv(
        file: PdfFile,
        onProgress: (Int) -> Unit,
    ): CareerPilotResult<String, NetworkError> = when (
        val result = safeCall<UserProfileDto> {
            client.submitFormWithBinaryData(
                url = Endpoints.ANALYZE_CV,
                formData = formData {
                    append(
                        key = "file",
                        value = file.bytes,
                        headers = Headers.build {
                            append(HttpHeaders.ContentType, PDF_MIME_TYPE)
                            append(
                                HttpHeaders.ContentDisposition,
                                "filename=\"${file.name.safeHeaderFileName()}\"",
                            )
                        },
                    )
                },
            ) {
                atsTimeout()
                onUpload { sent, total ->
                    onProgress(if (total == null || total <= 0L) 0 else (sent * 100L / total).toInt())
                }
            }
        }
    ) {
        is CareerPilotResult.Error -> result
        is CareerPilotResult.Success -> result.data.cvUrl?.takeIf(String::isNotBlank)?.let {
            CareerPilotResult.Success(it)
        } ?: CareerPilotResult.Error(NetworkError.EMPTY_RESULT)
    }

    override suspend fun importJob(url: String) = payload<JobWorkspaceDto> {
        client.post(Endpoints.ATS_IMPORT_JOB_URL) {
            atsTimeout()
            setBody(ImportJobRequestDto(url))
        }
    }

    override suspend fun getWorkspace(workspaceId: Long) = payload<JobWorkspaceDto> {
        client.get(Endpoints.atsWorkspace(workspaceId))
    }

    override suspend fun scoreCv(workspaceId: Long) = payload<AtsScoreDto> {
        client.post(Endpoints.atsScoreCv(workspaceId)) { atsTimeout() }
    }

    override suspend fun optimizeCv(workspaceId: Long) = payload<AiJobDto> {
        client.post(Endpoints.atsOptimizeCv(workspaceId))
    }

    override suspend fun getAiJob(jobId: Long) = payload<AiJobDto> {
        client.get(Endpoints.aiJob(jobId))
    }

    override suspend fun generateCoverLetter(workspaceId: Long) = payload<CoverLetterDto> {
        client.post(Endpoints.atsCoverLetter(workspaceId)) { atsTimeout() }
    }

    private suspend inline fun <reified T> payload(
        execute: () -> io.ktor.client.statement.HttpResponse,
    ): CareerPilotResult<T, NetworkError> = when (val response = safeCall<AtsApiResponseDto<T>>(execute)) {
        is CareerPilotResult.Error -> response
        is CareerPilotResult.Success -> response.data.data?.let { CareerPilotResult.Success(it) }
            ?: CareerPilotResult.Error(NetworkError.EMPTY_RESULT)
    }

    private fun io.ktor.client.request.HttpRequestBuilder.atsTimeout() {
        timeout {
            requestTimeoutMillis = ATS_TIMEOUT_MILLIS
            socketTimeoutMillis = ATS_TIMEOUT_MILLIS
        }
    }

    private fun String.safeHeaderFileName() = replace(UNSAFE_HEADER_CHARS, "_").take(MAX_FILE_NAME_LENGTH)

    private companion object {
        const val ATS_TIMEOUT_MILLIS = 120_000L
        const val PDF_MIME_TYPE = "application/pdf"
        const val MAX_FILE_NAME_LENGTH = 96
        val UNSAFE_HEADER_CHARS = Regex("[\\r\\n\\\"]")
    }
}
