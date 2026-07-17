package com.iti.onboarding.domain.usecase

import com.iti.onboarding.domain.repository.OnboardingRepository
import javax.inject.Inject

class GetTracksUseCase @Inject constructor(
    private val repository: OnboardingRepository
) {
    suspend operator fun invoke() = repository.getTracksUseCase()
}