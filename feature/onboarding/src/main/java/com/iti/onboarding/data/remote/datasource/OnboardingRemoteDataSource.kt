package com.iti.onboarding.data.remote.datasource

import com.iti.careerpilot.core.network.model.UpdateProfileRequestDto
import com.iti.careerpilot.core.network.model.UserResponseDto
import com.iti.core.model.PdfFile
import com.iti.onboarding.data.remote.dto.TracksResponseDto
import com.iti.onboarding.data.remote.dto.UploadFileResponseDto
import com.iti.onboarding.domain.model.FileUploadData

interface OnboardingRemoteDataSource {
    suspend fun uploadFile(fileData: FileUploadData): UploadFileResponseDto
    suspend fun updateProfile(request: UpdateProfileRequestDto): UserResponseDto
    suspend fun getTracks(): List<TracksResponseDto>
    suspend fun uploadCv(document: PdfFile): UploadFileResponseDto
}