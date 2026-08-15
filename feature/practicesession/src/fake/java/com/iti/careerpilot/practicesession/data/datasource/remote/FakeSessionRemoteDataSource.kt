package com.iti.careerpilot.practicesession.data.datasource.remote

import com.iti.careerpilot.practicesession.data.datasource.models.AnswerRequestDto
import com.iti.careerpilot.practicesession.data.datasource.models.AnswerResponseDto
import com.iti.careerpilot.practicesession.data.datasource.models.CreateSessionRequestDto
import com.iti.careerpilot.practicesession.data.datasource.models.CurrentQuestionDto
import com.iti.careerpilot.practicesession.data.datasource.models.FileUploadResponse
import com.iti.careerpilot.practicesession.data.datasource.models.OldSessionDto
import com.iti.careerpilot.practicesession.data.datasource.models.ScoreDto
import com.iti.careerpilot.practicesession.data.datasource.models.SessionDto
import com.iti.careerpilot.practicesession.data.datasource.models.SessionQuestionResultDto
import com.iti.careerpilot.practicesession.data.datasource.models.SessionResultDto
import com.iti.careerpilot.practicesession.domain.repo.SessionRemoteDataSource
import com.iti.common.error.NetworkError
import com.iti.common.result.CareerPilotResult
import com.iti.common.util.fakeDelay
import com.iti.common.util.shouldFail
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeSessionRemoteDataSource @Inject constructor() : SessionRemoteDataSource {

    private var questionCounter = 1
    private val totalQuestions = 8

    private val questions = listOf(
        "Can you describe the full Android Activity lifecycle from initial creation to final destruction, and explain in comprehensive detail how the system handles complex configuration changes such as screen rotation, fold/unfold transitions, and multi-window split screening? Furthermore, elaborate on the architectural differences between onSaveInstanceState, SavedStateHandle, and ViewModel retention, explain how the Android OS determines when to kill background processes under severe memory pressure, and provide a real-world scenario illustrating how you would recover volatile transient state after unexpected process termination without degrading user experience.",
        "What are Kotlin Coroutines, and how do they differ from traditional threads in Android? Explain structured concurrency, CoroutineScope lifecycle integration, dispatchers (Default, IO, Main), SupervisorJob, exception propagation and handling, and cooperative cancellation in large-scale production applications.",
        "Explain the difference between LiveData, Flow, and SharedFlow in the context of state management, detailing cold vs hot streams, backpressure handling, and lifecycle-aware collection in Jetpack Compose.",
        "How does Dagger Hilt simplify Dependency Injection in Android, what are its key component scopes, and how do custom qualifiers and Hilt viewmodel injection work under the hood?"
    )

    override suspend fun createNewSession(
        request: CreateSessionRequestDto
    ): CareerPilotResult<SessionDto, NetworkError> {
        fakeDelay()
        if (shouldFail()) return CareerPilotResult.Error(NetworkError.FAKE_SERVER_ERROR)
        
        questionCounter = 1
        return CareerPilotResult.Success(
            SessionDto(
                sessionId = 101L,
                trackName = "Android Developer",
                targetDurationMinutes = 15,
                maxQuestions = totalQuestions,
                startedAt = "2023-10-27T10:00:00Z",
                firstQuestion = CurrentQuestionDto(
                    id = 1L,
                    sessionId = 101L,
                    questionText = questions[0],
                    questionOrder = 1,
                    createdAt = "2023-10-27T10:00:01Z"
                )
            )
        )
    }

    override suspend fun uploadAudio(
        file: File,
        onProgress: (Int) -> Unit
    ): CareerPilotResult<FileUploadResponse, NetworkError> {
        fakeDelay()
        onProgress(100)
        if (shouldFail()) return CareerPilotResult.Error(NetworkError.FAKE_SERVER_ERROR)
        return CareerPilotResult.Success(
            FileUploadResponse(
                id = 999L,
                type = "audio/mpeg",
                originalName = file.name,
                url = "https://fake.storage/audio/${file.name}",
                sizeBytes = file.length(),
                createdAt = "2023-10-27T10:05:00Z"
            )
        )
    }

    override suspend fun submitAnswer(
        sessionId: Long,
        request: AnswerRequestDto
    ): CareerPilotResult<AnswerResponseDto, NetworkError> {
        fakeDelay(1000, 2000)
        if (shouldFail()) return CareerPilotResult.Error(NetworkError.FAKE_SERVER_ERROR)
        
        val answeredIndex = questionCounter - 1
        questionCounter++
        
        val status = if (questionCounter > totalQuestions) "READY_TO_COMPLETE" else "IN_PROGRESS"
        
        val nextQuestion = if (status == "IN_PROGRESS") {
            CurrentQuestionDto(
                id = questionCounter.toLong(),
                sessionId = sessionId,
                questionText = questions[questionCounter - 1],
                questionOrder = questionCounter,
                createdAt = "2023-10-27T10:10:00Z"
            )
        } else null

        return CareerPilotResult.Success(
            AnswerResponseDto(
                sessionStatus = status,
                score = ScoreDto(
                    id = 500L + questionCounter,
                    sessionQuestionId = answeredIndex.toLong() + 1,
                    contentRelevance = (70..95).random(),
                    clarity = (60..90).random(),
                    confidence = (75..95).random(),
                    pacing = (65..85).random(),
                    fillerWords = (1..5).random(),
                    overallScore = (75..90).random(),
                    coachingTip = "Your explanation of the concept was good, but try to provide a more concrete example from a past project.",
                    createdAt = "2023-10-27T10:11:00Z"
                ),
                nextQuestion = nextQuestion
            )
        )
    }

    override suspend fun getSessionFeedback(
        sessionId: Long
    ): CareerPilotResult<SessionResultDto, NetworkError> {
        fakeDelay(2000, 3000)
        if (shouldFail()) return CareerPilotResult.Error(NetworkError.FAKE_SERVER_ERROR)
        return CareerPilotResult.Success(
            SessionResultDto(
                id = 1L,
                sessionId = sessionId,
                overallScore = 85,
                clarityScore = 82,
                confidenceScore = 88,
                pacingScore = 80,
                fillerWordsScore = 92,
                contentRelevanceScore = 84,
                coachingTips = listOf(
                    "You demonstrated strong technical knowledge in Android internals.",
                    "Consider using the STAR method for behavioral questions in the future.",
                    "Work on reducing pause fillers like 'um' when explaining complex logic.",
                    "Great job on explaining Coroutines! Your clarity was exceptional there."
                ),
                generatedAt = "2023-10-27T11:00:00Z",
                createdAt = "2023-10-27T11:00:01Z",
                questions = questions.mapIndexed { index, questionText ->
                    SessionQuestionResultDto(
                        id = index.toLong() + 1,
                        sessionId = sessionId,
                        questionText = questionText,
                        questionOrder = index + 1,
                        userTranscript = "This is my fake answer for $questionText. I hope it sounds professional and insightful.",
                        durationMs = 45000L,
                        speechRateWpm = 120.0,
                        avgPauseMs = 300.0,
                        silenceRatio = 0.1,
                        createdAt = "2023-10-27T10:${10 + index}:00Z",
                        completedAt = "2023-10-27T10:${10 + index}:45Z",
                        score = ScoreDto(
                            id = 1000L + index,
                            sessionQuestionId = index.toLong() + 1,
                            contentRelevance = (70..95).random(),
                            clarity = (60..90).random(),
                            confidence = (75..95).random(),
                            pacing = (65..85).random(),
                            fillerWords = (1..5).random(),
                            overallScore = (75..90).random(),
                            coachingTip = "Your explanation of the concept was good, but try to provide a more concrete example from a past project.",
                            createdAt = "2023-10-27T10:${10 + index}:46Z"
                        )
                    )
                }
            )
        )
    }

    override suspend fun getSessionState(
        sessionId: Long
    ): CareerPilotResult<OldSessionDto, NetworkError> {
        fakeDelay()
        if (shouldFail()) return CareerPilotResult.Error(NetworkError.FAKE_SERVER_ERROR)
        return CareerPilotResult.Success(
            OldSessionDto(
                sessionId = sessionId,
                status = "IN_PROGRESS",
                trackName = "Android Developer",
                startedAt = "2023-10-27T10:00:00Z",
                updatedAt = "2023-10-27T10:15:00Z",
                answeredCount = questionCounter - 1,
                totalCount = totalQuestions,
                answeredQuestions = emptyList(),
                currentQuestion = if (questionCounter <= totalQuestions) {
                    CurrentQuestionDto(
                        id = questionCounter.toLong(),
                        sessionId = sessionId,
                        questionText = questions[questionCounter - 1],
                        questionOrder = questionCounter,
                        createdAt = "2023-10-27T10:15:01Z"
                    )
                } else null
            )
        )
    }
}
