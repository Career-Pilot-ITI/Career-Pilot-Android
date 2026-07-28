package com.iti.careerpilot.editprofile.data.datasource.remote

import com.iti.careerpilot.core.network.Endpoints
import com.iti.careerpilot.core.safecall.safeApiCall
import com.iti.careerpilot.editprofile.data.datasource.remote.models.FileUploadResponse
import com.iti.careerpilot.editprofile.data.datasource.remote.models.TrackDto
import com.iti.careerpilot.editprofile.data.datasource.remote.models.UpdateProfileRequestDto
import com.iti.careerpilot.editprofile.data.datasource.remote.models.UserProfileDto
import com.iti.careerpilot.editprofile.domain.datasource.remote.EditProfileRemoteDataSource
import com.iti.common.error.NetworkError
import com.iti.common.result.CareerPilotResult
import io.ktor.client.HttpClient
import io.ktor.client.plugins.onUpload
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.get
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
    ): CareerPilotResult<UserProfileDto, NetworkError> {
        return safeApiCall<UserProfileDto> {
            httpClient.patch(Endpoints.PROFILE) {
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
                url = Endpoints.UPLOAD_FILE,
                formData = formData {
                    append(
                        key = "file",
                        value = file.readBytes(),
                        headers = Headers.build {
                            append(HttpHeaders.ContentType, "image/jpeg")
                            append(HttpHeaders.ContentDisposition, "filename=\"${file.name}\"")
                        }
                    )
                    append("type", "avatars")
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

    override suspend fun uploadCV(
        file: File,
        onProgress: (Int) -> Unit
    ): CareerPilotResult<FileUploadResponse, NetworkError> {
        return safeApiCall<FileUploadResponse> {
            httpClient.submitFormWithBinaryData(
                url = Endpoints.UPLOAD_FILE,
                formData = formData {
                    append(
                        key = "file",
                        value = file.readBytes(),
                        headers = Headers.build {
                            append(HttpHeaders.ContentType, "application/pdf")
                            append(HttpHeaders.ContentDisposition, "filename=\"${file.name}\"")
                        }
                    )
                    append("type", "cvs")
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

    override suspend fun getTracks(): CareerPilotResult<List<TrackDto>, NetworkError> {
        return safeApiCall<List<TrackDto>> {
            httpClient.get(Endpoints.GET_TRACKS)
        }
    }
}
