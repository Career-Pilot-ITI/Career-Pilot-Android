package com.iti.careerpilot.quiz.data.remote

import com.iti.careerpilot.quiz.data.remote.dto.LearningPointDto
import com.iti.careerpilot.quiz.data.remote.dto.LearningPointResponseDto
import com.iti.careerpilot.quiz.data.remote.dto.QuizQuestionDto
import com.iti.careerpilot.quiz.data.remote.dto.QuizResponseDto
import com.iti.careerpilot.quiz.data.remote.dto.StudyTopicDto
import com.iti.careerpilot.quiz.data.remote.dto.TopicsResponseDto
import com.iti.common.util.fakeDelay
import com.iti.common.util.shouldFail
import javax.inject.Inject

class FakeQuizRemoteDataSource @Inject constructor(
) : QuizRemoteDataSource {
    override suspend fun generateTopics(track: String, seniority: String): TopicsResponseDto {
        fakeDelay()
        if (shouldFail()) throw Exception("Fake network error")
        return TopicsResponseDto(
            topics = listOf(
                StudyTopicDto("1", "Activity Lifecycle", "Understand states"),
                StudyTopicDto("2", "Fragments", "Learn navigation")
            )
        )
    }

    override suspend fun generateNextLearningPoint(
        track: String,
        seniority: String,
        topic: String,
        coveredConcepts: List<String>
    ): LearningPointResponseDto {
        fakeDelay()
        if (shouldFail()) throw Exception("Fake network error")
        return LearningPointResponseDto(
            topicCompleted = false,
            coveredConcept = "onCreate",
            learningPoint = LearningPointDto(
                title = "onCreate()",
                explanation = "Called when activity is first created.",
                example = "override fun onCreate(...) { ... }"
            )
        )
    }

    override suspend fun generateQuiz(
        topic: String,
        learningPointTitle: String,
        learningPointExplanation: String,
        learningPointExample: String
    ): QuizResponseDto {
        fakeDelay()
        if (shouldFail()) throw Exception("Fake network error")
        return QuizResponseDto(
            questions = listOf(
                QuizQuestionDto(
                    "q1",
                    "What is onCreate?",
                    listOf("Start", "Create", "Pause", "Stop"),
                    1,
                    "Correct"
                )
            )
        )
    }
}
