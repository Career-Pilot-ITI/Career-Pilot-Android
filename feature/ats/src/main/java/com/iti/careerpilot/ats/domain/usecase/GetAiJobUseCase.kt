package com.iti.careerpilot.ats.domain.usecase

import com.iti.careerpilot.ats.domain.repository.AtsRepository
import javax.inject.Inject

class GetAiJobUseCase @Inject constructor(
    private val repository: AtsRepository,
) {
    suspend operator fun invoke(jobId: Long) = repository.getAiJob(jobId)
}
