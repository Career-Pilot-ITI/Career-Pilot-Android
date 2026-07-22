package com.iti.careerpilot.editprofile.data.datasource.remote

import com.iti.careerpilot.editprofile.data.datasource.remote.models.FileUploadResponse
import com.iti.careerpilot.editprofile.data.datasource.remote.models.UpdateProfileRequestDto
import com.iti.careerpilot.editprofile.data.datasource.remote.models.UserProfileDto
import com.iti.careerpilot.editprofile.domain.datasource.remote.EditProfileRemoteDataSource
import com.iti.common.error.NetworkError
import com.iti.common.result.CareerPilotResult
import com.iti.common.util.fakeDelay
import com.iti.common.util.shouldFail
import java.io.File
import javax.inject.Inject

class FakeEditProfileRemoteDataSource @Inject constructor() : EditProfileRemoteDataSource {
    override suspend fun updateProfile(request: UpdateProfileRequestDto): CareerPilotResult<UserProfileDto, NetworkError> {
        fakeDelay()
        if (shouldFail()) return CareerPilotResult.Error(NetworkError.FAKE_SERVER_ERROR)
        return CareerPilotResult.Success(
            UserProfileDto(
                id = 1L,
                displayName = request.displayName,
                username = request.username,
                email = request.email,
                onboardingCompleted = true
            )
        )
    }

    override suspend fun uploadImage(file: File, onProgress: (Int) -> Unit): CareerPilotResult<FileUploadResponse, NetworkError> {
        fakeDelay()
        onProgress(100)
        if (shouldFail()) return CareerPilotResult.Error(NetworkError.FAKE_SERVER_ERROR)
        return CareerPilotResult.Success(
            FileUploadResponse(
                id = 1L,
                url = "https://fake.url/avatar.png",
                type = "IMAGE",
                originalName = file.name,
                sizeBytes = file.length(),
                createdAt = "2023-10-01T00:00:00Z"
            )
        )
    }

    override suspend fun uploadCV(file: File, onProgress: (Int) -> Unit): CareerPilotResult<FileUploadResponse, NetworkError> {
        fakeDelay()
        onProgress(100)
        if (shouldFail()) return CareerPilotResult.Error(NetworkError.FAKE_SERVER_ERROR)
        return CareerPilotResult.Success(
            FileUploadResponse(
                id = 2L,
                url = "https://fake.url/cv.pdf",
                type = "PDF",
                originalName = file.name,
                sizeBytes = file.length(),
                createdAt = "2023-10-01T00:00:00Z"
            )
        )
    }
}
