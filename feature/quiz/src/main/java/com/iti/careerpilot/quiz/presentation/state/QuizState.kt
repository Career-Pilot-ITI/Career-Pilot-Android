package com.iti.careerpilot.quiz.presentation.state

import androidx.compose.runtime.Immutable
import com.iti.careerpilot.quiz.domain.model.LearningPoint
import com.iti.careerpilot.quiz.domain.model.LearningQuiz
import com.iti.careerpilot.quiz.domain.model.StudyTopic
import com.iti.common.util.UIText

@Immutable
data class QuizState(
    val isLoading: Boolean = false,
    val error: UIText? = null,
    val trackName: String = "",
    val seniority: String = "",
    val isSeniorityLoaded: Boolean = false,
    val topics: List<StudyTopic> = emptyList(),
    val selectedTopic: StudyTopic? = null,
    val currentLearningPoint: LearningPoint? = null,
    val currentQuiz: LearningQuiz? = null,
    val coveredConcepts: Map<String, List<String>> = emptyMap(), // TopicId to CoveredConcepts
    val completedTopicIds: Set<String> = emptySet(),
    val currentStep: QuizStep = QuizStep.Topics,
    val quizAnswers: Map<Int, Int> = emptyMap(), // QuestionIndex to SelectedOptionIndex
    val showQuizResults: Boolean = false,
    val quizScore: Int = 0
)

sealed interface QuizStep {
    data object Loading : QuizStep
    data object Topics : QuizStep
    data object LearningPoint : QuizStep
    data object Quiz : QuizStep
    data object QuizResult : QuizStep
}
