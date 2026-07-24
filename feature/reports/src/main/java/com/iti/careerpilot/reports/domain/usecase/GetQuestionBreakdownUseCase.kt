package com.iti.careerpilot.reports.domain.usecase

import com.iti.careerpilot.reports.domain.repository.ReportsRepository
import javax.inject.Inject

class GetQuestionBreakdownUseCase @Inject constructor(
    private val repository: ReportsRepository,
) {
    suspend operator fun invoke(
        sessionId: Long,
    ) = repository.getQuestionBreakdown(sessionId)
}
