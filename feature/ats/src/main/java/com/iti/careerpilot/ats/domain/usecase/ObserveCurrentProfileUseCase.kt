package com.iti.careerpilot.ats.domain.usecase

import com.iti.careerpilot.ats.domain.repository.AtsRepository
import javax.inject.Inject

class ObserveCurrentProfileUseCase @Inject constructor(
    private val repository: AtsRepository,
) {
    operator fun invoke() = repository.userProfile
}
