package com.iti.careerpilot.ats.presentation.scoring

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iti.careerpilot.ats.domain.usecase.GetWorkspaceUseCase
import com.iti.careerpilot.ats.domain.usecase.ObserveCurrentProfileUseCase
import com.iti.careerpilot.ats.domain.usecase.ScoreCvUseCase
import com.iti.common.error.NetworkError
import com.iti.common.result.CareerPilotResult
import com.iti.common.util.toUIText
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ScoringViewModel @Inject constructor(
    private val getWorkspace: GetWorkspaceUseCase,
    private val scoreCv: ScoreCvUseCase,
    observeCurrentProfile: ObserveCurrentProfileUseCase,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val _state = MutableStateFlow(ScoringUiState())
    val state = _state.asStateFlow()

    private val effectChannel = Channel<ScoringEffect>(Channel.BUFFERED)
    val effects = effectChannel.receiveAsFlow()

    private var workspaceId: Long? = null
    private var activeOperation: Job? = null

    init {
        viewModelScope.launch {
            observeCurrentProfile().collect { profile ->
                _state.update { it.copy(trackId = profile.career.trackId) }
            }
        }
    }

    fun loadWorkspace(workspaceId: Long) {
        if (this.workspaceId == workspaceId && _state.value.workspace != null) return
        this.workspaceId = workspaceId
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            when (val result = getWorkspace(workspaceId)) {
                is CareerPilotResult.Error -> _state.update {
                    it.copy(isLoading = false, error = result.error.toUIText())
                }
                is CareerPilotResult.Success -> _state.update {
                    it.copy(
                        isLoading = false,
                        workspace = result.data,
                        wasInterrupted = savedStateHandle.get<Boolean>(attemptKey(workspaceId)) == true,
                    )
                }
            }
        }
    }

    fun onAction(action: ScoringAction) {
        when (action) {
            ScoringAction.RequestScore -> if (!_state.value.isLoading) {
                _state.update { it.copy(isScoreConfirmationVisible = true) }
            }
            ScoringAction.DismissConfirmation -> _state.update {
                it.copy(isScoreConfirmationVisible = false)
            }
            ScoringAction.ConfirmScore -> executeScore()
            ScoringAction.RetryWorkspace -> workspaceId?.let {
                this.workspaceId = null
                loadWorkspace(it)
            }
            ScoringAction.OpenCoins -> emit(ScoringEffect.OpenCoinsPaywall)
            ScoringAction.GenerateCoverLetter -> workspaceId?.let {
                emit(ScoringEffect.OpenCoverLetter(it))
            }
            ScoringAction.OptimizeCv -> workspaceId?.let {
                emit(ScoringEffect.OpenOptimizedCv(it))
            }
            ScoringAction.StartPractice -> _state.value.trackId?.let {
                emit(ScoringEffect.OpenPractice(it))
            }
        }
    }

    private fun executeScore() {
        val id = workspaceId ?: return
        if (_state.value.isLoading || activeOperation?.isActive == true) return
        savedStateHandle[attemptKey(id)] = true
        activeOperation = viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = true,
                    isScoreConfirmationVisible = false,
                    error = null,
                    hasInsufficientCoins = false,
                )
            }
            when (val result = scoreCv(id)) {
                is CareerPilotResult.Success -> {
                    savedStateHandle[attemptKey(id)] = false
                    _state.update {
                        it.copy(isLoading = false, score = result.data, wasInterrupted = false)
                    }
                }
                is CareerPilotResult.Error -> {
                    val interrupted = result.error == NetworkError.TIME_OUT
                    if (!interrupted) savedStateHandle[attemptKey(id)] = false
                    _state.update {
                        it.copy(
                            isLoading = false,
                            wasInterrupted = interrupted,
                            error = result.error.toUIText(),
                            hasInsufficientCoins = result.error == NetworkError.INSUFFICIENT_COINS,
                        )
                    }
                }
            }
        }
    }

    private fun emit(effect: ScoringEffect) {
        viewModelScope.launch { effectChannel.send(effect) }
    }

    private fun attemptKey(workspaceId: Long) = "ats_score_attempted_$workspaceId"
}
