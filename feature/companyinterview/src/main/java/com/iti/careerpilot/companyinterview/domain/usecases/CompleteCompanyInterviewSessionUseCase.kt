package com.iti.careerpilot.companyinterview.domain.usecases

import com.iti.careerpilot.companyinterview.domain.repository.CompanyInterviewRepository
import com.iti.common.error.NetworkError
import com.iti.common.result.CareerPilotResult
import javax.inject.Inject

class CompleteCompanyInterviewSessionUseCase @Inject constructor(
    private val repository: CompanyInterviewRepository
) {
    suspend operator fun invoke(token: String): CareerPilotResult<Boolean, NetworkError> {
        return repository.completeSession(token)
    }
}
