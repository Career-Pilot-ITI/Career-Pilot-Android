package com.iti.careerpilot.reports.domain.usecase

import com.iti.careerpilot.core.interviews.domain.repository.InterviewSessionRepository
import javax.inject.Inject

class GetSessionHistoryUseCase @Inject constructor(
    private val sessionRepository: InterviewSessionRepository,
) {
    suspend operator fun invoke(
        page: Int,
        size: Int,
    ) = sessionRepository.getSessions(page = page, size = size)
}
