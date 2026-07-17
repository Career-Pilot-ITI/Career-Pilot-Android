package com.iti.careerpilot.editprofile.data.datasource.remote

import com.iti.careerpilot.core.network.BuildConfig
import com.iti.careerpilot.core.safecall.safeApiCall
import com.iti.careerpilot.editprofile.data.datasource.remote.models.FileUploadResponse
import com.iti.careerpilot.editprofile.data.datasource.remote.models.UpdateProfileRequestDto
import com.iti.careerpilot.editprofile.data.datasource.remote.models.UpdateProfileResponseDto
import com.iti.careerpilot.editprofile.domain.datasource.remote.EditProfileRemoteDataSource
import com.iti.common.error.NetworkError
import com.iti.common.result.CareerPilotResult
import io.ktor.client.HttpClient
import io.ktor.client.plugins.onUpload
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.patch
import io.ktor.client.request.setBody
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import java.io.File
import javax.inject.Inject

class EditProfileRemoteDataSourceImpl @Inject constructor(
    private val httpClient: HttpClient
): EditProfileRemoteDataSource {

    override suspend fun updateProfile(
        request: UpdateProfileRequestDto
    ): CareerPilotResult<UpdateProfileResponseDto, NetworkError> {
        return safeApiCall<UpdateProfileResponseDto> {
            httpClient.patch("${BuildConfig.BASE_URL}api/v1/auth/profile") {
                setBody(request)
            }
        }
    }
    override suspend fun uploadImage(
        file: File,
        onProgress: (Int) -> Unit
    ): CareerPilotResult<FileUploadResponse, NetworkError> {
        return safeApiCall<FileUploadResponse> {
            httpClient.submitFormWithBinaryData(
                url = "${BuildConfig.BASE_URL}api/v1/files/upload",
                formData = formData {
                    append(
                        key = "file",
                        value = file.readBytes(),
                        headers = Headers.build {
                            append(HttpHeaders.ContentType, "image/jpeg")
                            append(HttpHeaders.ContentDisposition, "filename=\"${file.name}\"")
                        }
                    )
                    append("type", "AVATAR")
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
}