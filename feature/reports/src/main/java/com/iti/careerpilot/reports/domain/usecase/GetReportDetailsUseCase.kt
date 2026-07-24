package com.iti.careerpilot.reports.domain.usecase

import com.iti.careerpilot.reports.domain.repository.ReportsRepository
import javax.inject.Inject

class GetReportDetailsUseCase @Inject constructor(
    private val repository: ReportsRepository,
) {
    suspend operator fun invoke(
        sessionId: Long,
    ) = repository.getReportDetails(sessionId)
}
