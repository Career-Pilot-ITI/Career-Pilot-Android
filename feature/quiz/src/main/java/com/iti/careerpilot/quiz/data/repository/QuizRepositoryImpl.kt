package com.iti.careerpilot.quiz.data.repository

import com.iti.careerpilot.quiz.data.remote.QuizRemoteDataSource
import com.iti.careerpilot.quiz.domain.model.LearningPoint
import com.iti.careerpilot.quiz.domain.model.LearningPointResponse
import com.iti.careerpilot.quiz.domain.model.LearningQuiz
import com.iti.careerpilot.quiz.domain.model.QuizQuestion
import com.iti.careerpilot.quiz.domain.model.StudyTopic
import com.iti.careerpilot.quiz.domain.repository.QuizRepository
import com.iti.common.error.FirebaseError
import com.iti.common.result.CareerPilotResult
import com.iti.common.result.map
import javax.inject.Inject

class QuizRepositoryImpl @Inject constructor(
    private val remoteDataSource: QuizRemoteDataSource
) : QuizRepository {

    override suspend fun generateTopics(
        track: String,
        seniority: String
    ): CareerPilotResult<List<StudyTopic>, FirebaseError> {
        return remoteDataSource.generateTopics(track, seniority).map { response ->
            response.topics.map {
                StudyTopic(
                    id = it.id,
                    title = it.title,
                    description = it.description
                )
            }
        }
    }

    override suspend fun generateNextLearningPoint(
        track: String,
        seniority: String,
        topic: String,
        coveredConcepts: List<String>
    ): CareerPilotResult<LearningPointResponse, FirebaseError> {
        return remoteDataSource.generateNextLearningPoint(
            track, seniority, topic, coveredConcepts
        ).map { response ->
            LearningPointResponse(
                topicCompleted = response.topicCompleted,
                coveredConcept = response.coveredConcept,
                learningPoint = response.learningPoint?.let {
                    LearningPoint(
                        title = it.title,
                        explanation = it.explanation,
                        example = it.example
                    )
                }
            )
        }
    }

    override suspend fun generateQuiz(
        topic: String,
        learningPoint: LearningPoint
    ): CareerPilotResult<LearningQuiz, FirebaseError> {
        return remoteDataSource.generateQuiz(
            topic, learningPoint.title, learningPoint.explanation, learningPoint.example
        ).map { response ->
            LearningQuiz(
                questions = response.questions.map {
                    QuizQuestion(
                        id = it.id,
                        question = it.question,
                        options = it.options,
                        correctAnswerIndex = it.correctAnswerIndex,
                        explanation = it.explanation
                    )
                }
            )
        }
    }
}
