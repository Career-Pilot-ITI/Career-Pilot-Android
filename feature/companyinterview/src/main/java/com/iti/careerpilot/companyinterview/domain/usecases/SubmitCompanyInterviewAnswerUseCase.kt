package com.iti.careerpilot.companyinterview.domain.usecases

import com.iti.careerpilot.companyinterview.domain.models.ApplicantQuestion
import com.iti.careerpilot.companyinterview.domain.repository.CompanyInterviewRepository
import com.iti.common.error.NetworkError
import com.iti.common.result.CareerPilotResult
import javax.inject.Inject

class SubmitCompanyInterviewAnswerUseCase @Inject constructor(
    private val repository: CompanyInterviewRepository
) {
    suspend operator fun invoke(
        token: String,
        questionId: Long,
        transcript: String?,
        durationSeconds: Int,
        speechRateWpm: Double?,
        avgPauseMs: Double?,
        silenceRatio: Double?,
        fillerWordCount: Int,
        bodyLanguageMetrics: Map<String, String>?,
        proctoringFlags: Map<String, String>?
    ): CareerPilotResult<ApplicantQuestion?, NetworkError> {
        return repository.submitAnswer(
            token = token,
            questionId = questionId,
            transcript = transcript,
            durationSeconds = durationSeconds,
            speechRateWpm = speechRateWpm,
            avgPauseMs = avgPauseMs,
            silenceRatio = silenceRatio,
            fillerWordCount = fillerWordCount,
            bodyLanguageMetrics = bodyLanguageMetrics,
            proctoringFlags = proctoringFlags
        )
    }
}
