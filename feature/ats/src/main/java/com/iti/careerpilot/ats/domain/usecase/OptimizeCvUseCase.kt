package com.iti.careerpilot.ats.domain.usecase

import com.iti.careerpilot.ats.domain.model.AiJob
import com.iti.careerpilot.ats.domain.repository.AtsRepository
import com.iti.common.error.NetworkError
import com.iti.common.result.CareerPilotResult
import javax.inject.Inject

class OptimizeCvUseCase @Inject constructor(
    private val repository: AtsRepository,
) {
    suspend operator fun invoke(workspaceId: Long): CareerPilotResult<AiJob, NetworkError> =
        repository.optimizeCv(workspaceId)
}
