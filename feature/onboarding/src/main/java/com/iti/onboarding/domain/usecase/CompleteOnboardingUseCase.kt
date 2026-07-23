package com.iti.onboarding.domain.usecase

import com.iti.common.error.NetworkError
import com.iti.common.result.CareerPilotResult
import com.iti.onboarding.domain.repository.OnboardingRepository
import javax.inject.Inject

class CompleteOnboardingUseCase @Inject constructor(
    private val repository: OnboardingRepository,
) {
    suspend operator fun invoke(
        cvFileId: Long?,
    ): CareerPilotResult<Unit, NetworkError> = repository.completeOnboarding(cvFileId)
}
