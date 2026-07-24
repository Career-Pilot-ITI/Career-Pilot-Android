package com.iti.careerpilot.home.domain.usecase

import com.iti.careerpilot.home.domain.model.StartedSession
import com.iti.careerpilot.home.domain.repository.InterviewRepository
import com.iti.common.error.NetworkError
import com.iti.common.result.CareerPilotResult
import javax.inject.Inject

class StartInterviewSessionUseCase @Inject constructor(
    private val interviewRepository: InterviewRepository,
) {
    suspend operator fun invoke(trackId: Long): CareerPilotResult<StartedSession, NetworkError> =
        interviewRepository.startSession(trackId)
}
