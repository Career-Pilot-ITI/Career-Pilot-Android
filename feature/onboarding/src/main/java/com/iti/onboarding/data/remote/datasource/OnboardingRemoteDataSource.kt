package com.iti.onboarding.data.remote.datasource

import com.iti.onboarding.data.dto.UploadFileResponseDto
import com.iti.careerpilot.core.network.model.UpdateProfileRequestDto
import com.iti.careerpilot.core.network.model.UserResponseDto
import com.iti.onboarding.domain.model.FileUploadData
import com.iti.common.result.CareerPilotResult
import com.iti.onboarding.domain.error.TracksScreenError
import com.iti.onboarding.domain.model.Track

interface OnboardingRemoteDataSource {
    suspend fun uploadFile(fileData: FileUploadData): UploadFileResponseDto
    suspend fun updateProfile(request: UpdateProfileRequestDto): UserResponseDto
    suspend fun getTracks(): CareerPilotResult<List<Track>, TracksScreenError>
}