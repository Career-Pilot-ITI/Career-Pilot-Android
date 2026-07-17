package com.iti.onboarding.domain.repository

import com.iti.common.result.CareerPilotResult
import com.iti.onboarding.domain.error.TracksScreenError
import com.iti.onboarding.domain.model.Track
import com.iti.common.error.NetworkError
import com.iti.onboarding.domain.model.UploadedFile
import com.iti.onboarding.domain.model.FileUploadData
import com.iti.careerpilot.core.network.model.UpdateProfileRequestDto
import com.iti.careerpilot.core.network.model.UserResponseDto
import com.iti.core.model.PdfFile

interface OnboardingRepository {
    suspend fun uploadFile(fileData: FileUploadData): CareerPilotResult<UploadedFile, NetworkError>
    suspend fun saveAvatarUrl(url: String)
    suspend fun updateProfile(request: UpdateProfileRequestDto): CareerPilotResult<UserResponseDto, NetworkError>
    suspend fun getTracks(): CareerPilotResult<List<Track>, TracksScreenError>
    suspend fun uploadCv(
        document: PdfFile,
        onProgress: (Float) -> Unit,
    ): CareerPilotResult<Unit, NetworkError>
}
