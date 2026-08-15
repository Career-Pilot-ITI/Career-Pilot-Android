package com.iti.careerpilot.home.domain.usecase

import com.iti.careerpilot.core.interviews.domain.model.InterviewSession
import com.iti.careerpilot.core.interviews.domain.repository.InterviewSessionRepository
import com.iti.common.error.NetworkError
import com.iti.common.result.CareerPilotResult
import javax.inject.Inject

class GetInterviewSessionsUseCase @Inject constructor(
    private val sessionRepository: InterviewSessionRepository,
) {
    suspend operator fun invoke(
        limit: Int = DEFAULT_LIMIT,
    ): CareerPilotResult<List<InterviewSession>, NetworkError> =
        when (val result = sessionRepository.getSessions(page = FIRST_PAGE, size = limit)) {
            is CareerPilotResult.Error -> result
            is CareerPilotResult.Success -> CareerPilotResult.Success(result.data.sessions)
        }

    private companion object {
        const val FIRST_PAGE = 0
        const val DEFAULT_LIMIT = 10
    }
}
