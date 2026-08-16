package com.iti.careerpilot.ats.presentation.optimizedcv.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iti.careerpilot.ats.R
import com.iti.careerpilot.ats.domain.model.AiJobStatus
import com.iti.careerpilot.ats.domain.usecase.GetAiJobUseCase
import com.iti.careerpilot.ats.presentation.optimizedcv.state.OptimizedCvAction
import com.iti.careerpilot.ats.presentation.optimizedcv.state.OptimizedCvUiState
import com.iti.careerpilot.core.access.PlanAccessMap
import com.iti.careerpilot.core.access.domain.usecase.CheckFeatureAccessUseCase
import com.iti.careerpilot.core.access.domain.usecase.RefreshAccessUseCase
import com.iti.careerpilot.core.access.domain.usecase.handle
import com.iti.common.result.CareerPilotResult
import com.iti.common.util.UIText
import com.iti.common.util.toUIText
import com.iti.core.model.FeatureAccess
import com.iti.core.model.FeatureKey
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class OptimizedCvViewModel @Inject constructor(
    private val getAiJob: GetAiJobUseCase,
    private val checkFeatureAccess: CheckFeatureAccessUseCase,
    private val refreshAccess: RefreshAccessUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(OptimizedCvUiState())
    val state = _state.asStateFlow()

    private var jobId: Long? = null

    fun onAction(action: OptimizedCvAction) {
        when (action) {
            is OptimizedCvAction.Initial -> load(action.jobId)
            OptimizedCvAction.Retry -> jobId?.let(::load)
            OptimizedCvAction.DismissGateSheet -> _state.update { it.copy(showGateSheet = false) }
            OptimizedCvAction.DismissCoinTopUpSheet -> _state.update { it.copy(showCoinTopUpSheet = false) }
        }
    }

    private fun load(jobId: Long) {
        if (this.jobId == jobId && _state.value.sections.isNotEmpty()) return
        this.jobId = jobId
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            val access = checkFeatureAccess(FeatureKey.CvAiAnalysis).first()
            var handled = false
            access.handle(
                onGranted = { /* proceed — handled below */ },
                onLocked = { locked ->
                    handled = true
                    _state.update {
                        it.copy(
                            showGateSheet = true,
                            gatePlanFeatures = PlanAccessMap.featuresFor(locked.requiredPlan).map { f -> f.displayName() },
                            gateRequiredPlan = locked.requiredPlan,
                            gateFeatureName = FeatureKey.CvAiAnalysis.displayName(),
                            isLoading = false,
                        )
                    }
                },
                onCoinTopUpRequired = { coinReq ->
                    handled = true
                    _state.update {
                        it.copy(
                            showCoinTopUpSheet = true,
                            coinTopUpRequiredCost = coinReq.coinCost,
                            hasInsufficientCoins = true,
                            isLoading = false,
                        )
                    }
                },
                onStale = {
                    handled = true
                    refreshAccess()
                    _state.update { it.copy(isLoading = false) }
                },
            )
            if (handled) return@launch

            when (val result = getAiJob(jobId)) {
                is CareerPilotResult.Error -> _state.update {
                    it.copy(isLoading = false, error = result.error.toUIText())
                }
                is CareerPilotResult.Success -> {
                    val optimization = result.data.result
                    val error = when {
                        result.data.status == AiJobStatus.FAILED -> result.data.errorMessage
                            ?.let(UIText::DynamicString)
                            ?: UIText.StringResource(R.string.ats_cv_optimization_failed_message)
                        result.data.status != AiJobStatus.COMPLETED || optimization == null ->
                            UIText.StringResource(R.string.ats_cv_optimization_not_ready)
                        else -> null
                    }
                    _state.update {
                        it.copy(
                            sections = optimization?.sections ?: persistentListOf(),
                            recommendedTracks = optimization?.recommendedTracks ?: persistentListOf(),
                            coinCost = optimization?.coinCost,
                            isLoading = false,
                            error = error,
                        )
                    }
                }
            }
        }
    }
}
