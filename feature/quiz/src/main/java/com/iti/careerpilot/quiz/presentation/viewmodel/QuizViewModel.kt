package com.iti.careerpilot.quiz.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iti.careerpilot.quiz.domain.repository.QuizRepository
import com.iti.careerpilot.quiz.presentation.action.QuizAction
import com.iti.careerpilot.quiz.presentation.event.QuizEvent
import com.iti.careerpilot.quiz.presentation.state.QuizState
import com.iti.careerpilot.quiz.presentation.state.QuizStep
import com.iti.common.result.onError
import com.iti.common.result.onSuccess
import com.iti.common.util.toUIText
import com.iti.core.datastore.models.UserProfile
import com.iti.core.datastore.repo.UserProfileRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuizViewModel @Inject constructor(
    private val quizRepo: QuizRepository,
    private val userProfileRepo: UserProfileRepo
) : ViewModel() {

    private val _state = MutableStateFlow(QuizState())
    val state = _state.asStateFlow()

    private val _events = Channel<QuizEvent>()
    val events = _events.receiveAsFlow()

    init {
        observeProfile()
    }

    private fun observeProfile() {
        viewModelScope.launch {
            userProfileRepo.userProfile.collect { profile: UserProfile ->
                _state.update {
                    it.copy(
                        isSeniorityLoaded = true
                    )
                }
            }
        }
    }

    fun onAction(action: QuizAction) {
        when (action) {
            is QuizAction.Init -> {
                _state.update { it.copy(trackName = action.trackName) }
            }

            is QuizAction.SenioritySelected -> {
                _state.update { it.copy(seniority = action.level) }
                generateTopics()
            }

            is QuizAction.TopicSelected -> {
                _state.update { it.copy(selectedTopic = action.topic) }
                generateNextLearningPoint()
            }

            is QuizAction.AnswerSelected -> {
                _state.update {
                    it.copy(quizAnswers = it.quizAnswers + (action.questionIndex to action.optionIndex))
                }
            }

            QuizAction.SubmitQuiz -> {
                calculateScore()
                _state.update { it.copy(currentStep = QuizStep.QuizResult) }
            }

            QuizAction.StartQuiz -> {
                generateNextQuiz()
            }

            QuizAction.ContinueLearning -> {
                generateNextLearningPoint()
            }

            QuizAction.BackToTopics -> {
                _state.update { it.copy(currentStep = QuizStep.Topics, selectedTopic = null) }
            }

            QuizAction.BackToSeniority -> {
                _state.update { it.copy(currentStep = QuizStep.SelectSeniority, topics = emptyList()) }
            }

            QuizAction.Retry -> {
                when (_state.value.currentStep) {
                    QuizStep.Topics -> generateTopics()
                    QuizStep.LearningPoint -> generateNextLearningPoint()
                    QuizStep.Quiz -> generateNextQuiz()
                    else -> {}
                }
            }
        }
    }

    private fun generateTopics() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null, currentStep = QuizStep.Loading) }
            quizRepo.generateTopics(_state.value.trackName, _state.value.seniority)
                .onSuccess { topics ->
                    _state.update { it.copy(isLoading = false, topics = topics, currentStep = QuizStep.Topics) }
                }
                .onError { error ->
                    val uiText = error.toUIText()
                    _state.update { it.copy(isLoading = false, error = uiText) }
                    _events.send(QuizEvent.ShowError(uiText))
                }
        }
    }

    private fun generateNextLearningPoint() {
        val topic = _state.value.selectedTopic ?: return
        val covered = _state.value.coveredConcepts[topic.id] ?: emptyList()

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null, currentStep = QuizStep.Loading) }
            quizRepo.generateNextLearningPoint(
                _state.value.trackName,
                _state.value.seniority,
                topic.title,
                covered
            ).onSuccess { response ->
                if (response.topicCompleted) {
                    markTopicCompleted(topic.id)
                    _state.update { it.copy(isLoading = false, currentStep = QuizStep.Topics, selectedTopic = null) }
                } else {
                    val lp = response.learningPoint ?: return@onSuccess
                    val newCovered = if (response.coveredConcept != null) {
                        covered + response.coveredConcept
                    } else covered

                    _state.update {
                        it.copy(
                            isLoading = false,
                            currentLearningPoint = lp,
                            coveredConcepts = it.coveredConcepts + (topic.id to newCovered),
                            currentStep = QuizStep.LearningPoint
                        )
                    }
                }
            }.onError { error ->
                val uiText = error.toUIText()
                _state.update { it.copy(isLoading = false, error = uiText) }
                _events.send(QuizEvent.ShowError(uiText))
            }
        }
    }

    private fun generateNextQuiz() {
        val topic = _state.value.selectedTopic ?: return
        val lp = _state.value.currentLearningPoint ?: return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            quizRepo.generateQuiz(topic.title, lp)
                .onSuccess { quiz ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            currentQuiz = quiz,
                            quizAnswers = emptyMap(),
                            currentStep = QuizStep.Quiz
                        )
                    }
                }
                .onError { error ->
                    val uiText = error.toUIText()
                    _state.update { it.copy(isLoading = false, error = uiText) }
                    _events.send(QuizEvent.ShowError(uiText))
                }
        }
    }

    private fun calculateScore() {
        val quiz = _state.value.currentQuiz ?: return
        var score = 0
        quiz.questions.forEachIndexed { index, question ->
            if (_state.value.quizAnswers[index] == question.correctAnswerIndex) {
                score++
            }
        }
        _state.update { it.copy(quizScore = score) }
    }

    private fun markTopicCompleted(topicId: String) {
        _state.update {
            it.copy(
                completedTopicIds = it.completedTopicIds + topicId,
                topics = it.topics.map { t ->
                    if (t.id == topicId) t.copy(progress = 100, isCompleted = true) else t
                }
            )
        }
    }
}
