package com.iti.onboarding.data.remote.datasource

import com.iti.careerpilot.core.network.Endpoints
import com.iti.careerpilot.core.network.model.UpdateProfileRequestDto
import com.iti.careerpilot.core.network.model.UserProfileDto
import com.iti.onboarding.data.remote.dto.TracksResponseDto
import com.iti.onboarding.data.remote.dto.UploadFileResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.onUpload
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import java.io.File
import javax.inject.Inject

class OnboardingRemoteDataSourceImpl @Inject constructor(
    private val httpClient: HttpClient,
) : OnboardingRemoteDataSource {
    override suspend fun uploadFile(
        file: File,
        onProgress: (Int) -> Unit
    ): UploadFileResponseDto {
        return httpClient.submitFormWithBinaryData(
            url = Endpoints.UPLOAD_FILE,
            formData = formData {
                append("type", "avatars")
                append(
                    "file",
                    file.readBytes(),
                    Headers.build {
                        append(HttpHeaders.ContentType, "image/jpeg")
                        append(HttpHeaders.ContentDisposition, "filename=\"${file.name}\"")
                    }
                )
            }
        ) {
            onUpload { bytesSentTotal, contentLength ->
                val total = contentLength ?: 0L
                val progress = if (total > 0) {
                    ((bytesSentTotal.toDouble() / total.toDouble()) * 100).toInt()
                } else 0
                onProgress(progress)
            }
        }.body()
    }

    override suspend fun updateProfile(request: UpdateProfileRequestDto): UserProfileDto {
        return httpClient.patch(Endpoints.PROFILE) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    override suspend fun getTracks(): List<TracksResponseDto> {
        return httpClient.get(Endpoints.GET_TRACKS) {
            contentType(ContentType.Application.Json)
        }.body()
    }

    override suspend fun uploadCv(
        file: File,
        onProgress: (Int) -> Unit
    ): UploadFileResponseDto {
        return httpClient.submitFormWithBinaryData(
            url = Endpoints.UPLOAD_FILE,
            formData = formData {
                append("type", "cvs")
                append(
                    "file",
                    file.readBytes(),
                    Headers.build {
                        append(HttpHeaders.ContentType, "application/pdf")
                        append(HttpHeaders.ContentDisposition, "filename=\"${file.name}\"")
                    }
                )
            }
        ) {
            onUpload { bytesSentTotal, contentLength ->
                val total = contentLength ?: 0L
                val progress = if (total > 0) {
                    ((bytesSentTotal.toDouble() / total.toDouble()) * 100).toInt()
                } else 0
                onProgress(progress)
            }
        }.body()
    }
}
