package com.iti.onboarding.domain.usecase

import com.iti.common.error.NetworkError
import com.iti.common.result.CareerPilotResult
import com.iti.onboarding.domain.repository.OnboardingRepository
import com.iti.careerpilot.core.network.model.UpdateProfileRequestDto
import com.iti.careerpilot.core.network.model.UserResponseDto
import javax.inject.Inject

class UpdateProfileUseCase @Inject constructor(
    private val repository: OnboardingRepository,
) {
    suspend operator fun invoke(
        request: UpdateProfileRequestDto
    ): CareerPilotResult<UserResponseDto, NetworkError> = repository.updateProfile(request)
}
