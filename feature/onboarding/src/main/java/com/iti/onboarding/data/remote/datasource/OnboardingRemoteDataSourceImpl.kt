package com.iti.onboarding.data.remote.datasource

import com.iti.careerpilot.core.network.Endpoints
import com.iti.careerpilot.core.network.model.UpdateProfileRequestDto
import com.iti.careerpilot.core.network.model.UserResponseDto
import com.iti.common.result.CareerPilotResult
import com.iti.core.model.PdfFile
import com.iti.onboarding.data.dto.UploadFileResponseDto
import com.iti.onboarding.domain.error.TracksScreenError
import com.iti.onboarding.domain.model.FileUploadData
import com.iti.onboarding.domain.model.Track
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.patch
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

class OnboardingRemoteDataSourceImpl @Inject constructor(
    private val httpClient: HttpClient,
) : OnboardingRemoteDataSource {
    override suspend fun uploadFile(
        fileData: FileUploadData
    ): UploadFileResponseDto {
        return httpClient.submitFormWithBinaryData(
            url = Endpoints.UPLOAD_FILE,
            formData = formData {
                append("type", "avatars")
                append(
                    "file",
                    fileData.bytes,
                    Headers.build {
                        append(HttpHeaders.ContentType, fileData.mimeType)
                        append(HttpHeaders.ContentDisposition, "filename=\"${fileData.fileName}\"")
                    }
                )
            }
        ).body()
    }

    override suspend fun updateProfile(request: UpdateProfileRequestDto): UserResponseDto {
        return httpClient.patch(Endpoints.UPDATE_PROFILE) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    override suspend fun getTracks(): CareerPilotResult<List<Track>, TracksScreenError> {
        delay(1000)
        return CareerPilotResult.Success(
            listOf(
                Track("1", "Android Development"),
                Track("2", "iOS Development"),
                Track("3", "Web Development"),
                Track("4", "Data Science"),
                Track("5", "Machine Learning"),
                Track("6", "UI/UX Design"),
                Track("7", "Other")
            )
        )
    }

    override suspend fun uploadCv(
        document: PdfFile,
        onProgress: (Float) -> Unit,
    ): UploadFileResponseDto {
        return coroutineScope {
            launch {
                val totalBytes = document.bytes.size.coerceAtLeast(1)
                var uploadedBytes = 0

                while (uploadedBytes < totalBytes) {
                    delay(UPLOAD_PROGRESS_DELAY_MS)
                    uploadedBytes = minOf(
                        uploadedBytes + UPLOAD_CHUNK_SIZE_BYTES,
                        totalBytes,
                    )
                    onProgress(uploadedBytes.toFloat() / totalBytes.toFloat())
                }
            }

            async<UploadFileResponseDto> {
                return@async httpClient.submitFormWithBinaryData(
                    url = Endpoints.UPLOAD_FILE,
                    formData = formData {
                        append("type", "cvs")
                        append(
                            "file",
                            document.bytes,
                            Headers.build {
                                append(HttpHeaders.ContentType, document.mimeType)
                                append(HttpHeaders.ContentDisposition, "filename=\"${document.name}\"")
                            }
                        )
                    }
                ).body()
            }.await()
        }
    }

    private companion object {
        const val UPLOAD_CHUNK_SIZE_BYTES = 256 * 1024
        const val UPLOAD_PROGRESS_DELAY_MS = 40L
    }
}