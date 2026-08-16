package com.iti.careerpilot.quiz.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iti.careerpilot.core.access.PlanAccessMap
import com.iti.careerpilot.core.access.domain.AccessRepository
import com.iti.careerpilot.core.access.domain.usecase.CheckFeatureAccessUseCase
import com.iti.careerpilot.core.access.domain.usecase.RefreshAccessUseCase
import com.iti.careerpilot.core.access.domain.usecase.handle
import com.iti.careerpilot.quiz.domain.repository.QuizRepository
import com.iti.careerpilot.quiz.presentation.action.QuizAction
import com.iti.careerpilot.quiz.presentation.event.QuizEvent
import com.iti.careerpilot.quiz.presentation.state.QuizState
import com.iti.careerpilot.quiz.presentation.state.QuizStep
import com.iti.careerpilot.quiz.presentation.state.RetryType
import com.iti.careerpilot.quiz.R
import com.iti.common.result.onError
import com.iti.common.result.onSuccess
import com.iti.common.util.UIText
import com.iti.common.util.toUIText
import com.iti.core.model.FeatureAccess
import com.iti.core.model.FeatureKey
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
    private val checkFeatureAccess: CheckFeatureAccessUseCase,
    private val refreshAccess: RefreshAccessUseCase,
    private val accessRepository: AccessRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(QuizState())
    val state = _state.asStateFlow()

    private val _events = Channel<QuizEvent>()
    val events = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            checkFeatureAccess(FeatureKey.Quizzes).collect { access ->
                _state.update {
                    it.copy(
                        quizAccess = access,
                        gatePlanFeatures = if (access is FeatureAccess.Locked) {
                            PlanAccessMap.featuresFor(access.requiredPlan).map { f -> f.displayName() }
                        } else it.gatePlanFeatures,
                        gateRequiredPlan = if (access is FeatureAccess.Locked) access.requiredPlan else it.gateRequiredPlan
                    )
                }
                if (access is FeatureAccess.StaleCacheBlocked) {
                    refreshAccess()
                }
            }
        }

        viewModelScope.launch {
            accessRepository.accessState.collect { accessState ->
                _state.update {
                    it.copy(
                        coinBalance = accessState.coinBalance,
                        planDisplayName = accessState.plan.displayName(),
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
                _state.value.quizAccess.handle(
                    onGranted = {
                        _state.update { it.copy(seniority = action.level) }
                        generateTopics()
                    },
                    onLocked = { locked ->
                        _state.update {
                            it.copy(
                                showGateSheet = true,
                                gatePlanFeatures = PlanAccessMap.featuresFor(locked.requiredPlan).map { f -> f.displayName() },
                                gateRequiredPlan = locked.requiredPlan,
                            )
                        }
                    },
                    onCoinTopUpRequired = { coinReq ->
                        _state.update {
                            it.copy(
                                showCoinTopUpSheet = true,
                                coinTopUpRequiredCost = coinReq.coinCost,
                            )
                        }
                    },
                    onStale = {
                        viewModelScope.launch { refreshAccess() }
                    },
                    onUnknown = {
                        val cost = _state.value.quizCoinCost
                        if (_state.value.coinBalance < cost) {
                            _state.update {
                                it.copy(
                                    showCoinTopUpSheet = true,
                                    coinTopUpRequiredCost = cost,
                                )
                            }
                        } else {
                            _state.update { it.copy(seniority = action.level) }
                            generateTopics()
                        }
                    },
                )
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
                when (_state.value.retryType) {
                    RetryType.GENERATE_TOPICS -> generateTopics()
                    RetryType.GENERATE_LEARNING_POINT -> generateNextLearningPoint()
                    RetryType.GENERATE_QUIZ -> generateNextQuiz()
                    null -> {}
                }
            }

            QuizAction.DismissGateSheet -> _state.update { it.copy(showGateSheet = false) }
            QuizAction.DismissCoinTopUpSheet -> _state.update { it.copy(showCoinTopUpSheet = false) }
            QuizAction.UpgradeFromGate -> {
                _state.update { it.copy(showGateSheet = false) }
                viewModelScope.launch { _events.send(QuizEvent.NavigateToPaywall(false)) }
            }
            QuizAction.BuyCoinsClicked -> {
                _state.update { it.copy(showCoinTopUpSheet = false) }
                viewModelScope.launch { _events.send(QuizEvent.NavigateToPaywall(true)) }
            }
        }
    }

    private fun generateTopics() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = true,
                    loadingMessage = UIText.StringResource(R.string.quiz_loading_topics),
                    currentStep = QuizStep.Loading,
                    error = null,
                    retryType = null,
                )
            }
            quizRepo.generateTopics(_state.value.trackName, _state.value.seniority)
                .onSuccess { topics ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            loadingMessage = null,
                            topics = topics,
                            currentStep = QuizStep.Topics
                        )
                    }
                }
                .onError { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = error.toUIText(),
                            retryType = RetryType.GENERATE_TOPICS,
                            currentStep = QuizStep.Error
                        )
                    }
                }
        }
    }

    private fun generateNextLearningPoint() {
        val topic = _state.value.selectedTopic ?: return
        val covered = _state.value.coveredConcepts[topic.id] ?: emptyList()

        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = true,
                    loadingMessage = UIText.StringResource(R.string.quiz_loading_learning_point),
                    currentStep = QuizStep.Loading,
                    error = null,
                    retryType = null,
                )
            }
            quizRepo.generateNextLearningPoint(
                _state.value.trackName,
                _state.value.seniority,
                topic.title,
                covered
            ).onSuccess { response ->
                if (response.topicCompleted) {
                    markTopicCompleted(topic.id)
                    _state.update {
                        it.copy(
                            isLoading = false,
                            loadingMessage = null,
                            currentStep = QuizStep.Topics,
                            selectedTopic = null
                        )
                    }
                } else {
                    val lp = response.learningPoint ?: return@onSuccess
                    val newCovered = if (response.coveredConcept != null) {
                        covered + response.coveredConcept
                    } else covered

                    _state.update {
                        it.copy(
                            isLoading = false,
                            loadingMessage = null,
                            currentLearningPoint = lp,
                            coveredConcepts = it.coveredConcepts + (topic.id to newCovered),
                            currentStep = QuizStep.LearningPoint
                        )
                    }
                }
            }.onError { error ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        loadingMessage = null,
                        error = error.toUIText(),
                        retryType = RetryType.GENERATE_LEARNING_POINT,
                        currentStep = QuizStep.Error
                    )
                }
            }
        }
    }

    private fun generateNextQuiz() {
        val topic = _state.value.selectedTopic ?: return
        val lp = _state.value.currentLearningPoint ?: return

        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = true,
                    loadingMessage = UIText.StringResource(R.string.quiz_loading_quiz),
                    error = null,
                    retryType = null
                )
            }
            quizRepo.generateQuiz(topic.title, lp)
                .onSuccess { quiz ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            loadingMessage = null,
                            currentQuiz = quiz,
                            quizAnswers = emptyMap(),
                            currentStep = QuizStep.Quiz
                        )
                    }
                }
                .onError { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            loadingMessage = null,
                            error = error.toUIText(),
                            retryType = RetryType.GENERATE_QUIZ,
                            currentStep = QuizStep.Error
                        )
                    }
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
