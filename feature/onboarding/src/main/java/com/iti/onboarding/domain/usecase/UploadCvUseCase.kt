package com.iti.onboarding.domain.usecase

import com.iti.onboarding.domain.model.CvDocument
import com.iti.onboarding.domain.repository.CvRepository
import javax.inject.Inject

class UploadCvUseCase @Inject constructor(
    private val repository: CvRepository,
) {
    suspend operator fun invoke(
        document: CvDocument,
        onProgress: (Float) -> Unit,
    ) = repository.uploadCv(
        document = document,
        onProgress = onProgress,
    )
}
