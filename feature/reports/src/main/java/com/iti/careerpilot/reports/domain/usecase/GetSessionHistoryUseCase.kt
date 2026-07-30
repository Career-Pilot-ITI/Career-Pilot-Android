package com.iti.careerpilot.reports.domain.usecase

import com.iti.careerpilot.reports.domain.repository.ReportsRepository
import javax.inject.Inject

class GetSessionHistoryUseCase @Inject constructor(
    private val repository: ReportsRepository,
) {
    suspend operator fun invoke(
        page: Int,
        size: Int,
    ) = repository.getSessionHistoryPage(page = page, size = size)
}
