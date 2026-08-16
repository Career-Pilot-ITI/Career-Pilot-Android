package com.iti.careerpilot.quiz.presentation.state

import androidx.compose.runtime.Immutable
import com.iti.careerpilot.core.access.FeaturePricingMap
import com.iti.careerpilot.quiz.domain.model.LearningPoint
import com.iti.careerpilot.quiz.domain.model.LearningQuiz
import com.iti.careerpilot.quiz.domain.model.StudyTopic
import com.iti.common.util.UIText
import com.iti.core.model.FeatureAccess
import com.iti.core.model.FeatureKey
import com.iti.core.model.Plan

@Immutable
data class QuizState(
    val isLoading: Boolean = false,
    val loadingMessage: UIText? = null,
    val error: UIText? = null,
    val retryType: RetryType? = null,
    val trackName: String = "",
    val seniority: String = "",
    val topics: List<StudyTopic> = emptyList(),
    val selectedTopic: StudyTopic? = null,
    val currentLearningPoint: LearningPoint? = null,
    val currentQuiz: LearningQuiz? = null,
    val coveredConcepts: Map<String, List<String>> = emptyMap(), // TopicId to CoveredConcepts
    val completedTopicIds: Set<String> = emptySet(),
    val currentStep: QuizStep = QuizStep.SelectSeniority,
    val quizAnswers: Map<Int, Int> = emptyMap(), // QuestionIndex to SelectedOptionIndex
    val showQuizResults: Boolean = false,
    val quizScore: Int = 0,
    val quizCoinCost: Int = FeaturePricingMap.coinCost(FeatureKey.Quizzes),
    val quizAccess: FeatureAccess = FeatureAccess.Unknown,
    val coinBalance: Int = 0,
    val planDisplayName: String = "Free",
    val showGateSheet: Boolean = false,
    val gatePlanFeatures: List<String> = emptyList(),
    val gateRequiredPlan: Plan = Plan.PLUS,
    val showCoinTopUpSheet: Boolean = false,
    val coinTopUpRequiredCost: Int = 0,
)

sealed interface QuizStep {
    data object Loading : QuizStep
    data object SelectSeniority : QuizStep
    data object Topics : QuizStep
    data object LearningPoint : QuizStep
    data object Quiz : QuizStep
    data object QuizResult : QuizStep
    data object Error : QuizStep
}

enum class RetryType {
    GENERATE_TOPICS,
    GENERATE_LEARNING_POINT,
    GENERATE_QUIZ
}
