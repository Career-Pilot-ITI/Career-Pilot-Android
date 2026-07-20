package com.iti.onboarding.data.remote.datasource

import com.iti.careerpilot.core.network.model.UpdateProfileRequestDto
import com.iti.careerpilot.core.network.model.UserResponseDto
import com.iti.onboarding.data.remote.dto.TracksResponseDto
import com.iti.onboarding.data.remote.dto.UploadFileResponseDto
import java.io.File

interface OnboardingRemoteDataSource {
    suspend fun uploadFile(
        file: File,
        onProgress: (Int) -> Unit
    ): UploadFileResponseDto

    suspend fun updateProfile(request: UpdateProfileRequestDto): UserResponseDto
    suspend fun getTracks(): List<TracksResponseDto>

    suspend fun uploadCv(
        file: File,
        onProgress: (Int) -> Unit
    ): UploadFileResponseDto
}