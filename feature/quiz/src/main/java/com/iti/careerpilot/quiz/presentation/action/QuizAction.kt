package com.iti.careerpilot.quiz.presentation.action

import com.iti.careerpilot.quiz.domain.model.StudyTopic

sealed interface QuizAction {
    data class Init(val trackName: String) : QuizAction
    data class SenioritySelected(val level: String) : QuizAction
    data class TopicSelected(val topic: StudyTopic) : QuizAction
    data object BackToTopics : QuizAction
    data object BackToSeniority : QuizAction
    data object ContinueLearning : QuizAction
    data object StartQuiz : QuizAction
    data class AnswerSelected(val questionIndex: Int, val optionIndex: Int) : QuizAction
    data object SubmitQuiz : QuizAction
    data object Retry : QuizAction
}
