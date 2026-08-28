package com.iti.careerpilot.companyinterview

import com.iti.careerpilot.companyinterview.data.datasource.CompanyInterviewRemoteDataSource
import com.iti.careerpilot.companyinterview.data.models.ApplicantInterviewMetadataDto
import com.iti.careerpilot.companyinterview.data.models.ApplicantQuestionDto
import com.iti.careerpilot.companyinterview.data.models.StartApplicantSessionResponseDto
import com.iti.careerpilot.companyinterview.data.models.SubmitApplicantAnswerResponseDto
import com.iti.careerpilot.companyinterview.data.repository.CompanyInterviewRepositoryImpl
import com.iti.common.error.NetworkError
import com.iti.common.result.CareerPilotResult
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CompanyInterviewRepositoryTest {

    private lateinit var fakeDataSource: FakeCompanyInterviewRemoteDataSource
    private lateinit var repository: CompanyInterviewRepositoryImpl

    @Before
    fun setup() {
        fakeDataSource = FakeCompanyInterviewRemoteDataSource()
        repository = CompanyInterviewRepositoryImpl(fakeDataSource)
    }

    @Test
    fun getMetadata_returns_mapped_domain_model() = runTest {
        val result = repository.getMetadata("valid_token")
        assertTrue(result is CareerPilotResult.Success)
        val data = (result as CareerPilotResult.Success).data
        assertEquals("valid_token", data.token)
        assertEquals("Tech Corp", data.companyName)
        assertEquals(3, data.totalQuestions)
    }

    @Test
    fun startSession_returns_mapped_session_state() = runTest {
        val result = repository.startSession("valid_token")
        assertTrue(result is CareerPilotResult.Success)
        val state = (result as CareerPilotResult.Success).data
        assertEquals(101L, state.sessionId)
        assertEquals(1L, state.currentQuestion?.questionId)
        assertEquals(3, state.totalQuestions)
    }

    @Test
    fun submitAnswer_returns_next_question() = runTest {
        val result = repository.submitAnswer(
            token = "valid_token",
            questionId = 1L,
            transcript = "Sample answer",
            durationSeconds = 45,
            speechRateWpm = 130.0,
            avgPauseMs = 300.0,
            silenceRatio = 0.1,
            fillerWordCount = 1,
            bodyLanguageMetrics = null,
            proctoringFlags = null
        )
        assertTrue(result is CareerPilotResult.Success)
        val nextQ = (result as CareerPilotResult.Success).data
        assertEquals(2L, nextQ?.questionId)
    }
}

private class FakeCompanyInterviewRemoteDataSource : CompanyInterviewRemoteDataSource {
    override suspend fun getMetadata(token: String) = CareerPilotResult.Success(
        ApplicantInterviewMetadataDto(
            token = token,
            interviewId = 42L,
            title = "Senior Android Engineer",
            deadline = "2026-09-01T00:00:00Z",
            companyName = "Tech Corp",
            totalQuestions = 3,
            applicantEmail = "applicant@test.com",
            emailVerified = true
        )
    )

    override suspend fun verifyEmail(token: String, email: String) = getMetadata(token)

    override suspend fun startSession(token: String) = CareerPilotResult.Success(
        StartApplicantSessionResponseDto(
            sessionId = 101L,
            totalQuestions = 3,
            currentQuestion = ApplicantQuestionDto(
                questionId = 1L,
                questionOrder = 1,
                questionText = "Explain Coroutines",
                timeLimitSeconds = 90,
                totalQuestions = 3,
                isLastQuestion = false
            )
        )
    )

    override suspend fun submitAnswer(token: String, request: com.iti.careerpilot.companyinterview.data.models.SubmitApplicantAnswerRequestDto) =
        CareerPilotResult.Success(
            SubmitApplicantAnswerResponseDto(
                nextQuestion = ApplicantQuestionDto(
                    questionId = 2L,
                    questionOrder = 2,
                    questionText = "Explain MVI vs MVVM",
                    timeLimitSeconds = 90,
                    totalQuestions = 3,
                    isLastQuestion = false
                )
            )
        )

    override suspend fun completeSession(token: String) =
        CareerPilotResult.Success(SubmitApplicantAnswerResponseDto(sessionCompleted = true))

    override suspend fun getSessionState(token: String) =
        CareerPilotResult.Success(
            com.iti.careerpilot.companyinterview.data.models.ApplicantSessionRecoveryResponseDto(
                sessionId = 101L,
                status = "IN_PROGRESS",
                currentQuestionIndex = 1,
                answeredQuestionsCount = 1,
                totalQuestions = 3
            )
        )
}
