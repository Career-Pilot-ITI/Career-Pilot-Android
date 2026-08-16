package com.iti.careerpilot.ats.presentation.jobdetails.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iti.careerpilot.ats.domain.usecase.GetWorkspaceUseCase
import com.iti.careerpilot.ats.presentation.jobdetails.state.JobDetailsAction
import com.iti.careerpilot.ats.presentation.jobdetails.state.JobDetailsEffect
import com.iti.careerpilot.ats.presentation.jobdetails.state.JobDetailsUiState
import com.iti.careerpilot.core.access.PlanAccessMap
import com.iti.careerpilot.core.access.domain.AccessRepository
import com.iti.careerpilot.core.access.domain.usecase.CheckFeatureAccessUseCase
import com.iti.careerpilot.core.access.domain.usecase.RefreshAccessUseCase
import com.iti.careerpilot.core.access.domain.usecase.handle
import com.iti.common.result.CareerPilotResult
import com.iti.common.util.toUIText
import com.iti.core.model.FeatureAccess
import com.iti.core.model.FeatureKey
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class JobDetailsViewModel @Inject constructor(
    private val getWorkspace: GetWorkspaceUseCase,
    private val checkFeatureAccess: CheckFeatureAccessUseCase,
    private val refreshAccess: RefreshAccessUseCase,
    private val accessRepository: AccessRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(JobDetailsUiState())
    val state = _state.asStateFlow()

    private val effectChannel = Channel<JobDetailsEffect>(Channel.BUFFERED)
    val effects = effectChannel.receiveAsFlow()

    private var workspaceId: Long? = null

    init {
        observeAccess()
    }

    private fun observeAccess() {
        viewModelScope.launch {
            checkFeatureAccess(FeatureKey.AtsFeatures).collect { access ->
                _state.update {
                    it.copy(
                        atsScoreAccess = access,
                        gatePlanFeatures = if (access is FeatureAccess.Locked) {
                            PlanAccessMap.featuresFor(access.requiredPlan).map { f -> f.displayName() }
                        } else it.gatePlanFeatures,
                        gateRequiredPlan = if (access is FeatureAccess.Locked) access.requiredPlan else it.gateRequiredPlan,
                    )
                }
                if (access is FeatureAccess.StaleCacheBlocked) {
                    refreshAccess()
                }
            }
        }

        viewModelScope.launch {
            checkFeatureAccess(FeatureKey.CvAiAnalysis).collect { access ->
                _state.update { it.copy(cvOptimizeAccess = access) }
                if (access is FeatureAccess.StaleCacheBlocked) {
                    refreshAccess()
                }
            }
        }

        viewModelScope.launch {
            checkFeatureAccess(FeatureKey.CoverLetter).collect { access ->
                _state.update { it.copy(coverLetterAccess = access) }
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

    fun onAction(action: JobDetailsAction) {
        when (action) {
            is JobDetailsAction.Initial -> loadWorkspace(action.workspaceId)
            JobDetailsAction.Retry -> workspaceId?.let(::loadWorkspace)
            JobDetailsAction.StartScoring -> onStartScoring()
            JobDetailsAction.DismissGateSheet -> _state.update { it.copy(showGateSheet = false) }
            JobDetailsAction.DismissCoinTopUpSheet -> _state.update { it.copy(showCoinTopUpSheet = false) }
            JobDetailsAction.UpgradeFromGate -> {
                _state.update { it.copy(showGateSheet = false) }
                viewModelScope.launch { effectChannel.send(JobDetailsEffect.NavigateToPaywall(false)) }
            }
            JobDetailsAction.BuyCoinsClicked -> {
                _state.update { it.copy(showCoinTopUpSheet = false) }
                viewModelScope.launch { effectChannel.send(JobDetailsEffect.NavigateToPaywall(true)) }
            }
        }
    }

    private fun onStartScoring() {
        val id = workspaceId ?: return
        _state.value.atsScoreAccess.handle(
            onGranted = {
                viewModelScope.launch {
                    effectChannel.send(JobDetailsEffect.OpenScore(id))
                }
            },
            onLocked = { locked ->
                _state.update {
                    it.copy(
                        showGateSheet = true,
                        gatePlanFeatures = PlanAccessMap.featuresFor(locked.requiredPlan).map { f -> f.displayName() },
                        gateRequiredPlan = locked.requiredPlan,
                        gateFeatureName = FeatureKey.AtsFeatures.displayName(),
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
                viewModelScope.launch {
                    refreshAccess()
                }
            },
        )
    }

    private fun loadWorkspace(workspaceId: Long) {
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
                        workspace = result.data,
                        isLoading = false,
                        error = null,
                    )
                }
            }
        }
    }
}
