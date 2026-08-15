package com.iti.careerpilot.practicesession.presentation.resultscreen

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iti.careerpilot.ai.cache.InMemorySessionCache
import com.iti.careerpilot.ai.domain.EvaluateBodyLanguageUseCase
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
    private val evaluateBodyLanguageUseCase: EvaluateBodyLanguageUseCase,
    private val sessionCache: InMemorySessionCache,
    @Dispatcher(IO) private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {

    private val sessionId: Long? get() = savedStateHandle.get<Long>("sessionId")

    private val _state = MutableStateFlow(ResultState(sessionId = sessionId))
    val state = _state.asStateFlow()

    init {
        loadResult()
        loadBodyLanguage()
    }

    fun initialise(id: Long) {
        if (savedStateHandle.contains("sessionId") && savedStateHandle.get<Long>("sessionId") == id) return
        savedStateHandle["sessionId"] = id
        _state.update { it.copy(sessionId = id) }
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
        val id = sessionId ?: return
        val cachedEval = sessionCache.getEvaluation(id)
        val metrics = sessionCache.getMetrics(id)

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
            evaluateBodyLanguage(id, metrics)
        } else {
            _state.update { it.copy(bodyLanguageUiState = BodyLanguageUiState.Idle) }
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
        val id = sessionId ?: return
        viewModelScope.launch(ioDispatcher) {
            _state.update { it.copy(isLoading = true) }
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
        }
    }
}