package com.iti.onboarding.domain.usecase

import com.iti.core.datastore.models.UserProfile
import com.iti.onboarding.domain.repository.OnboardingRepository
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class GetUserProfileUseCase @Inject constructor(
    private val repository: OnboardingRepository,
) {
    operator fun invoke(): StateFlow<UserProfile> {
        return repository.userProfile
    }
}
