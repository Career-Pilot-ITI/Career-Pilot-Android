package com.iti.careerpilot.home.domain.usecase

import com.iti.careerpilot.home.domain.model.InterviewSession
import com.iti.careerpilot.home.domain.repository.InterviewRepository
import com.iti.common.error.NetworkError
import com.iti.common.result.CareerPilotResult
import javax.inject.Inject

class GetInterviewSessionsUseCase @Inject constructor(
    private val interviewRepository: InterviewRepository,
) {
    suspend operator fun invoke(): CareerPilotResult<List<InterviewSession>, NetworkError> =
        interviewRepository.getInterviewSessions()
}
