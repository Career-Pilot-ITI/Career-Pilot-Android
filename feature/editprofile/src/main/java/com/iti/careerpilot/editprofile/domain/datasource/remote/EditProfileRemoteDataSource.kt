package com.iti.careerpilot.editprofile.domain.datasource.remote


import com.iti.careerpilot.editprofile.data.datasource.remote.models.FileUploadResponse
import com.iti.careerpilot.editprofile.data.datasource.remote.models.TrackDto
import com.iti.careerpilot.editprofile.data.datasource.remote.models.UpdateProfileRequestDto
import com.iti.careerpilot.editprofile.data.datasource.remote.models.UserProfileDto
import com.iti.common.error.NetworkError
import com.iti.common.result.CareerPilotResult
import java.io.File

interface EditProfileRemoteDataSource {

    suspend fun updateProfile(
        request: UpdateProfileRequestDto
    ): CareerPilotResult<UserProfileDto, NetworkError>

    suspend fun uploadImage(
        file: File,
        onProgress: (Int) -> Unit
    ): CareerPilotResult<FileUploadResponse, NetworkError>

    suspend fun uploadCV(
        file: File,
        onProgress: (Int) -> Unit
    ): CareerPilotResult<FileUploadResponse, NetworkError>

    suspend fun getTracks(): CareerPilotResult<List<TrackDto>, NetworkError>
}
