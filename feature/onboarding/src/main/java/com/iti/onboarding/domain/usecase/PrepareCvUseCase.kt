package com.iti.onboarding.domain.usecase

import com.iti.onboarding.domain.repository.CvRepository
import javax.inject.Inject

class PrepareCvUseCase @Inject constructor(
    private val repository: CvRepository,
) {
    suspend operator fun invoke(uri: String) = repository.preparePdf(uri)
}
