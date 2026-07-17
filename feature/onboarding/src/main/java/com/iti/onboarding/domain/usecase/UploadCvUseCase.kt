package com.iti.onboarding.domain.usecase

import com.iti.core.model.PdfFile
import com.iti.onboarding.domain.repository.OnboardingRepository
import javax.inject.Inject

class UploadCvUseCase @Inject constructor(
    private val repository: OnboardingRepository,
) {
    suspend operator fun invoke(
        document: PdfFile,
        onProgress: (Float) -> Unit,
    ) = repository.uploadCv(
        document = document,
        onProgress = onProgress,
    )
}
