package com.iti.careerpilot.quiz.presentation.action

import com.iti.careerpilot.quiz.domain.model.StudyTopic

sealed interface QuizIntent {
    data class Init(val trackName: String) : QuizIntent
    data class SenioritySelected(val level: String) : QuizIntent
    data class TopicSelected(val topic: StudyTopic) : QuizIntent
    data object BackToTopics : QuizIntent
    data object BackToSeniority : QuizIntent
    data object ContinueLearning : QuizIntent
    data object StartQuiz : QuizIntent
    data class AnswerSelected(val questionIndex: Int, val optionIndex: Int) : QuizIntent
    data object SubmitQuiz : QuizIntent
    data object Retry : QuizIntent
    data object DismissGateSheet : QuizIntent
    data object DismissCoinTopUpSheet : QuizIntent
    data object UpgradeFromGate : QuizIntent
    data object BuyCoinsClicked : QuizIntent
}
