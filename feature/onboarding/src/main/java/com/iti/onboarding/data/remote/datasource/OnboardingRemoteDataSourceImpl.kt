package com.iti.onboarding.data.remote.datasource

import com.iti.onboarding.data.dto.UploadFileResponseDto
import com.iti.careerpilot.core.network.model.UpdateProfileRequestDto
import com.iti.careerpilot.core.network.model.UserResponseDto
import com.iti.onboarding.domain.model.FileUploadData
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
import javax.inject.Inject
import com.iti.careerpilot.core.network.Endpoints
import com.iti.common.result.CareerPilotResult
import com.iti.onboarding.domain.error.TracksScreenError
import com.iti.onboarding.domain.model.Track

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
        TODO("Not yet implemented")
    }
}