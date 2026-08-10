package com.iti.careerpilot.ats.presentation.optimizedcv

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iti.careerpilot.ats.domain.usecase.GetWorkspaceUseCase
import com.iti.careerpilot.ats.domain.usecase.OptimizeCvUseCase
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
class OptimizedCvViewModel @Inject constructor(
    private val getWorkspace: GetWorkspaceUseCase,
    private val optimizeCv: OptimizeCvUseCase,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val _state = MutableStateFlow(OptimizedCvUiState())
    val state = _state.asStateFlow()
    private val effectChannel = Channel<OptimizedCvEffect>(Channel.BUFFERED)
    val effects = effectChannel.receiveAsFlow()
    private var workspaceId: Long? = null
    private var activeOperation: Job? = null

    fun loadWorkspace(workspaceId: Long) {
        if (this.workspaceId == workspaceId && !_state.value.isLoading) return
        this.workspaceId = workspaceId
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            when (val result = getWorkspace(workspaceId)) {
                is CareerPilotResult.Error -> _state.update {
                    it.copy(isLoading = false, error = result.error.toUIText())
                }
                is CareerPilotResult.Success -> _state.update {
                    it.copy(
                        optimizedText = result.data.cvOptimizedText.orEmpty(),
                        isLoading = false,
                        wasInterrupted = savedStateHandle.get<Boolean>(attemptKey(workspaceId)) == true,
                    )
                }
            }
        }
    }

    fun onAction(action: OptimizedCvAction) {
        when (action) {
            OptimizedCvAction.RequestOptimization -> if (!_state.value.isLoading) {
                _state.update { it.copy(isConfirmationVisible = true) }
            }
            OptimizedCvAction.DismissConfirmation -> _state.update {
                it.copy(isConfirmationVisible = false)
            }
            OptimizedCvAction.ConfirmOptimization -> executeOptimization()
            OptimizedCvAction.Copy -> _state.value.optimizedText.takeIf(String::isNotBlank)?.let {
                emit(OptimizedCvEffect.CopyText(it))
            }
            OptimizedCvAction.OpenCoins -> emit(OptimizedCvEffect.OpenCoinsPaywall)
        }
    }

    private fun executeOptimization() {
        val id = workspaceId ?: return
        if (_state.value.isLoading || activeOperation?.isActive == true) return
        savedStateHandle[attemptKey(id)] = true
        activeOperation = viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = true,
                    isConfirmationVisible = false,
                    hasInsufficientCoins = false,
                    error = null,
                )
            }
            when (val result = optimizeCv(id)) {
                is CareerPilotResult.Success -> {
                    savedStateHandle[attemptKey(id)] = false
                    _state.update {
                        it.copy(
                            optimizedText = result.data.optimizedCv,
                            recommendedTracks = result.data.recommendedTracks,
                            coinCost = result.data.coinCost,
                            isLoading = false,
                            wasInterrupted = false,
                        )
                    }
                }
                is CareerPilotResult.Error -> {
                    val interrupted = result.error == NetworkError.TIME_OUT
                    if (!interrupted) savedStateHandle[attemptKey(id)] = false
                    _state.update {
                        it.copy(
                            isLoading = false,
                            wasInterrupted = interrupted,
                            hasInsufficientCoins = result.error == NetworkError.INSUFFICIENT_COINS,
                            error = result.error.toUIText(),
                        )
                    }
                }
            }
        }
    }

    private fun emit(effect: OptimizedCvEffect) {
        viewModelScope.launch { effectChannel.send(effect) }
    }

    private fun attemptKey(id: Long) = "ats_optimize_attempted_$id"
}
