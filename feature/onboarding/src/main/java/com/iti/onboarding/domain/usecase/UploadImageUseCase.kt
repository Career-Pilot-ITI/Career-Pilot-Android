package com.iti.onboarding.domain.usecase

import com.iti.common.error.NetworkError
import com.iti.common.result.CareerPilotResult
import com.iti.onboarding.domain.model.UploadedFile
import com.iti.onboarding.domain.model.FileUploadData
import com.iti.onboarding.domain.repository.OnboardingRepository
import javax.inject.Inject

class UploadImageUseCase @Inject constructor(
    private val repository: OnboardingRepository,
) {
    suspend operator fun invoke(
        fileData: FileUploadData
    ): CareerPilotResult<UploadedFile, NetworkError> = repository.uploadFile(fileData)
}

