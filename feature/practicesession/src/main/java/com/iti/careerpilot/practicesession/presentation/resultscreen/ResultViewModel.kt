package com.iti.careerpilot.practicesession.presentation.resultscreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iti.careerpilot.ai.domain.EvaluateBodyLanguageUseCase
import com.iti.careerpilot.practicesession.domain.repo.SessionRepo
import com.iti.common.result.onError
import com.iti.common.result.onSuccess
import com.iti.common.snackbar.CareerPilotSnackbarController
import com.iti.common.util.toUIText
import com.iti.core.model.bodylanguage.BodyLanguageMetrics
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ResultViewModel @Inject constructor(
    private val sessionRepo: SessionRepo,
    private val evaluateBodyLanguageUseCase: EvaluateBodyLanguageUseCase,
) : ViewModel() {

    private var hasLoadedInitialData = false

    private val _state = MutableStateFlow(ResultState())
    val state = _state
        .onStart {
            if (!hasLoadedInitialData) {
                loadResult()
                hasLoadedInitialData = true
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = ResultState()
        )

    fun onAction(action: ResultAction) {
        when (action) {
            is ResultAction.UpdateSessionId -> {
                val metrics = action.bodyLanguageMetricsJson?.let { json ->
                    try {
                        kotlinx.serialization.json.Json.decodeFromString(
                            BodyLanguageMetrics.serializer(),
                            json
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
                _state.update {
                    it.copy(
                        sessionId = action.sessionId,
                        bodyLanguageMetrics = metrics,
                    )
                }
                loadResult()

                if (metrics != null) {
                    evaluateBodyLanguage(action.sessionId, metrics)
                }
            }

            ResultAction.RefreshResult -> loadResult()
        }
    }

    private fun evaluateBodyLanguage(
        sessionId: Long,
        metrics: BodyLanguageMetrics,
    ) {
        viewModelScope.launch {
            _state.update { it.copy(isEvaluatingBodyLanguage = true) }
            val (evaluation, fallbackReason) = evaluateBodyLanguageUseCase(sessionId, metrics)
            _state.update {
                it.copy(
                    isEvaluatingBodyLanguage = false,
                    bodyLanguageEvaluation = evaluation,
                    bodyLanguageFallbackReason = fallbackReason,
                )
            }
        }
    }

    private fun loadResult() {
        viewModelScope.launch {
            val currentState = state.value
            currentState.sessionId?.let { sessionId ->
                _state.update {
                    it.copy(
                        isLoading = true
                    )
                }
                sessionRepo.getSessionFeedback(sessionId)
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
                            it.copy(
                                isLoading = false
                            )
                        }
                        CareerPilotSnackbarController.show(error.toUIText())
                    }
            }
        }
    }
}