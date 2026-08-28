package com.iti.careerpilot.companyinterview.domain.usecases

import com.iti.careerpilot.companyinterview.domain.models.CompanyInterviewMetadata
import com.iti.careerpilot.companyinterview.domain.repository.CompanyInterviewRepository
import com.iti.common.error.NetworkError
import com.iti.common.result.CareerPilotResult
import javax.inject.Inject

class VerifyApplicantEmailUseCase @Inject constructor(
    private val repository: CompanyInterviewRepository
) {
    suspend operator fun invoke(token: String, email: String): CareerPilotResult<CompanyInterviewMetadata, NetworkError> {
        return repository.verifyEmail(token, email)
    }
}
