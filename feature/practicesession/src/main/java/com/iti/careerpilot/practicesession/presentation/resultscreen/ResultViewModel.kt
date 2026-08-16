package com.iti.careerpilot.practicesession.presentation.resultscreen

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iti.careerpilot.ai.cache.InMemorySessionCache
import com.iti.careerpilot.ai.domain.EvaluateBodyLanguageUseCase
import com.iti.careerpilot.challengefirestore.ChallengeFirestoreDataSource
import com.iti.careerpilot.challengefirestore.ChallengeSession
import com.iti.careerpilot.practicesession.domain.models.Score
import com.iti.careerpilot.practicesession.domain.models.SessionQuestionResult
import com.iti.careerpilot.practicesession.domain.models.SessionResult
import com.iti.careerpilot.practicesession.domain.repo.SessionRepo
import com.iti.common.result.onError
import com.iti.common.result.onSuccess
import com.iti.common.snackbar.CareerPilotSnackbarController
import com.iti.common.util.toUIText
import com.iti.core.model.bodylanguage.BodyLanguageMetrics
import com.iti.common.dispatcher.CareerPilotDispatchers.IO
import com.iti.common.dispatcher.Dispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ResultViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val sessionRepo: SessionRepo,
    private val firestoreDataSource: ChallengeFirestoreDataSource,
    private val evaluateBodyLanguageUseCase: EvaluateBodyLanguageUseCase,
    private val sessionCache: InMemorySessionCache,
    @Dispatcher(IO) private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {

    private val sessionId: Long? get() = savedStateHandle.get<Long>("sessionId")
    private val firestoreSessionId: String? get() = savedStateHandle.get<String>("firestoreSessionId")

    private val _state = MutableStateFlow(
        ResultState(
            sessionId = sessionId,
            firestoreSessionId = firestoreSessionId
        )
    )
    val state = _state.asStateFlow()

    init {
        loadResult()
        loadBodyLanguage()
    }

    fun initialise(id: Long) {
        if (savedStateHandle.contains("sessionId") && savedStateHandle.get<Long>("sessionId") == id) return
        savedStateHandle.remove<String>("firestoreSessionId")
        savedStateHandle["sessionId"] = id
        _state.update { it.copy(sessionId = id, firestoreSessionId = null) }
        loadResult()
        loadBodyLanguage()
    }

    fun initialise(id: String) {
        if (savedStateHandle.contains("firestoreSessionId") && savedStateHandle.get<String>("firestoreSessionId") == id) return
        savedStateHandle.remove<Long>("sessionId")
        savedStateHandle["firestoreSessionId"] = id
        _state.update { it.copy(firestoreSessionId = id, sessionId = null) }
        loadResult()
        loadBodyLanguage()
    }

    fun onAction(action: ResultAction) {
        when (action) {
            ResultAction.RefreshResult -> {
                loadResult()
                loadBodyLanguage()
            }
        }
    }

    private fun loadBodyLanguage() {
        val id = sessionId
        val fsId = firestoreSessionId

        if (id == null && fsId == null) return

        val cachedEval = if (fsId != null) sessionCache.getEvaluation(fsId) else id?.let {
            sessionCache.getEvaluation(it)
        }
        val metrics = if (fsId != null) sessionCache.getMetrics(fsId) else id?.let {
            sessionCache.getMetrics(it)
        }

        _state.update { it.copy(bodyLanguageMetrics = metrics) }

        if (cachedEval != null) {
            val (evaluation, fallbackReason) = cachedEval
            val uiState = if (fallbackReason != null) {
                BodyLanguageUiState.FallbackUsed(evaluation, fallbackReason)
            } else {
                BodyLanguageUiState.Success(evaluation)
            }
            _state.update { it.copy(bodyLanguageUiState = uiState) }
        } else if (metrics != null) {
            if (fsId != null) {
                evaluateFirestoreBodyLanguage(fsId, metrics)
            } else if (id != null) {
                evaluateBodyLanguage(id, metrics)
            }
        } else {
            _state.update { it.copy(bodyLanguageUiState = BodyLanguageUiState.Idle) }
        }
    }

    private fun evaluateFirestoreBodyLanguage(
        sessionId: String,
        metrics: BodyLanguageMetrics,
    ) {
        viewModelScope.launch(ioDispatcher) {
            _state.update { it.copy(bodyLanguageUiState = BodyLanguageUiState.Loading) }
            val (evaluation, fallbackReason) = evaluateBodyLanguageUseCase(sessionId, metrics)
            val uiState = if (fallbackReason != null) {
                BodyLanguageUiState.FallbackUsed(evaluation, fallbackReason)
            } else {
                BodyLanguageUiState.Success(evaluation)
            }
            _state.update {
                it.copy(bodyLanguageUiState = uiState)
            }
        }
    }

    private fun evaluateBodyLanguage(
        sessionId: Long,
        metrics: BodyLanguageMetrics,
    ) {
        viewModelScope.launch(ioDispatcher) {
            _state.update { it.copy(bodyLanguageUiState = BodyLanguageUiState.Loading) }
            val (evaluation, fallbackReason) = evaluateBodyLanguageUseCase(sessionId, metrics)
            val uiState = if (fallbackReason != null) {
                BodyLanguageUiState.FallbackUsed(evaluation, fallbackReason)
            } else {
                BodyLanguageUiState.Success(evaluation)
            }
            _state.update {
                it.copy(bodyLanguageUiState = uiState)
            }
        }
    }

    private fun loadResult() {
        val id = sessionId
        val fsId = firestoreSessionId

        viewModelScope.launch(ioDispatcher) {
            _state.update { it.copy(isLoading = true) }

            if (fsId != null) {
                firestoreDataSource.getSession(fsId)
                    .onSuccess { challengeSession ->
                        _state.update {
                            it.copy(
                                sessionResult = mapToSessionResult(challengeSession),
                                isLoading = false
                            )
                        }
                    }
                    .onError { error ->
                        _state.update { it.copy(isLoading = false) }
                        CareerPilotSnackbarController.show(error.toUIText())
                    }
            } else if (id != null) {
                sessionRepo.getSessionFeedback(id)
                    .onSuccess { sessionResult ->
                        _state.update {
                            it.copy(
                                sessionResult = sessionResult,
                                isLoading = false
                            )
                        }
                    }
                    .onError { error ->
                        _state.update {
                            it.copy(isLoading = false)
                        }
                        CareerPilotSnackbarController.show(error.toUIText())
                    }
            } else {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun mapToSessionResult(session: ChallengeSession): SessionResult {
        return SessionResult(
            id = 0, // Placeholder for Long ID
            sessionId = 0, // Placeholder
            participantName = session.participantName,
            participantEmail = session.participantEmail,
            overallScore = session.overallScore ?: 0,
            clarityScore = session.clarityScore ?: 0,
            confidenceScore = session.confidenceScore ?: 0,
            pacingScore = session.pacingScore ?: 0,
            fillerWordsScore = session.fillerWordsScore ?: 0,
            contentRelevanceScore = session.contentRelevanceScore ?: 0,
            coachingTips = session.coachingTips,
            generatedAt = session.updatedAt,
            createdAt = session.startedAt,
            questions = session.results.map { q ->
                SessionQuestionResult(
                    id = 0,
                    sessionId = 0,
                    questionText = q.questionText,
                    questionOrder = q.questionOrder,
                    userTranscript = q.userTranscript,
                    durationMs = q.durationMs,
                    speechRateWpm = q.speechRateWpm,
                    avgPauseMs = q.avgPauseMs,
                    silenceRatio = q.silenceRatio,
                    createdAt = q.createdAt,
                    completedAt = q.completedAt,
                    score = q.score?.let { s ->
                        Score(
                            id = 0,
                            sessionQuestionId = 0,
                            contentRelevance = s.contentRelevance,
                            clarity = s.clarity,
                            confidence = s.confidence,
                            pacing = s.pacing,
                            fillerWords = s.fillerWords,
                            overallScore = s.overallScore,
                            coachingTip = s.coachingTip,
                            createdAt = s.createdAt
                        )
                    }
                )
            }
        )
    }
}