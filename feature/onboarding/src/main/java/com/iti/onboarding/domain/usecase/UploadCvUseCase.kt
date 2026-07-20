package com.iti.onboarding.domain.usecase

import android.net.Uri
import com.iti.common.error.NetworkError
import com.iti.common.result.CareerPilotResult
import com.iti.onboarding.domain.model.UploadedFile
import com.iti.onboarding.domain.repository.OnboardingRepository
import javax.inject.Inject

class UploadCvUseCase @Inject constructor(
    private val repository: OnboardingRepository,
) {
    suspend operator fun invoke(
        uri: Uri,
        onProgress: (Int) -> Unit = {}
    ): CareerPilotResult<UploadedFile, NetworkError> = repository.uploadCv(uri, onProgress)
}
