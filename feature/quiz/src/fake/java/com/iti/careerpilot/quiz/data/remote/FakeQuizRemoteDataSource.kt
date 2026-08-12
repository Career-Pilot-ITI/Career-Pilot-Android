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
                StudyTopicDto("1", "Activity Lifecycle", "Understanding activity states and transitions"),
                StudyTopicDto("2", "Fragments", "Learning modular UI and fragment transactions"),
                StudyTopicDto("3", "Dependency Injection", "Managing dependencies with Hilt/Dagger"),
                StudyTopicDto("4", "Jetpack Compose", "Building modern declarative UIs"),
                StudyTopicDto("5", "Coroutines & Flow", "Handling asynchronous programming and reactive streams"),
                StudyTopicDto("6", "WorkManager", "Scheduling background tasks effectively"),
                StudyTopicDto("7", "ViewModel & LiveData", "Managing UI-related data in a lifecycle-conscious way"),
                StudyTopicDto("8", "Data Persistence (Room)", "Local database management and caching strategies")
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
                    "What is the first callback in the Activity lifecycle?",
                    listOf("onStart()", "onCreate()", "onResume()", "onPause()"),
                    1,
                    "onCreate() is the first callback triggered when an activity is created."
                ),
                QuizQuestionDto(
                    "q2",
                    "Which component is used to manage UI-related data in a lifecycle-aware way?",
                    listOf("Activity", "Service", "ViewModel", "Fragment"),
                    2,
                    "ViewModel is designed to store and manage UI-related data so that the data survives configuration changes."
                ),
                QuizQuestionDto(
                    "q3",
                    "What is the recommended way to handle background tasks that need to be guaranteed to run?",
                    listOf("AsyncTask", "Thread", "WorkManager", "IntentService"),
                    2,
                    "WorkManager is the recommended solution for persistent work."
                ),
                QuizQuestionDto(
                    "q4",
                    "Which layout is most suitable for building complex, declarative UIs in modern Android?",
                    listOf("LinearLayout", "RelativeLayout", "ConstraintLayout", "Jetpack Compose"),
                    3,
                    "Jetpack Compose is Android's modern toolkit for building native UI."
                )
            )
        )
    }
}
