package com.iti.careerpilot.ats.presentation.scoring.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iti.careerpilot.ats.domain.usecase.GetWorkspaceUseCase
import com.iti.careerpilot.ats.domain.usecase.ObserveCurrentProfileUseCase
import com.iti.careerpilot.ats.domain.usecase.OptimizeCvUseCase
import com.iti.careerpilot.ats.domain.usecase.ScoreCvUseCase
import com.iti.careerpilot.ats.R
import com.iti.careerpilot.ats.presentation.scoring.state.ScoringIntent
import com.iti.careerpilot.ats.presentation.scoring.state.ScoringEffect
import com.iti.careerpilot.ats.presentation.scoring.state.ScoringUiState
import com.iti.careerpilot.core.access.PlanAccessMap
import com.iti.careerpilot.core.access.domain.AccessRepository
import com.iti.careerpilot.core.access.domain.usecase.CheckFeatureAccessUseCase
import com.iti.careerpilot.core.access.domain.usecase.RefreshAccessUseCase
import com.iti.careerpilot.core.access.domain.usecase.handle
import com.iti.common.error.NetworkError
import com.iti.common.result.CareerPilotResult
import com.iti.common.util.toUIText
import com.iti.common.util.UIText
import com.iti.core.model.FeatureAccess
import com.iti.core.model.FeatureKey
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ScoringViewModel @Inject constructor(
    private val getWorkspace: GetWorkspaceUseCase,
    private val scoreCv: ScoreCvUseCase,
    private val observeCurrentProfile: ObserveCurrentProfileUseCase,
    private val optimizeCv: OptimizeCvUseCase,
    private val savedStateHandle: SavedStateHandle,
    private val checkFeatureAccess: CheckFeatureAccessUseCase,
    private val refreshAccess: RefreshAccessUseCase,
    private val accessRepository: AccessRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(ScoringUiState())
    val state = _state.asStateFlow()

    private val effectChannel = Channel<ScoringEffect>(Channel.BUFFERED)
    val effects = effectChannel.receiveAsFlow()

    private var workspaceId: Long? = null
    private var activeOperation: Job? = null
    private var profileObservationJob: Job? = null
    private var optimizationOperation: Job? = null
    private var accessObservationJob: Job? = null

    init {
        observeAccess()
    }

    private fun observeAccess() {
        if (accessObservationJob != null) return
        accessObservationJob = viewModelScope.launch {
            launch {
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
            launch {
                checkFeatureAccess(FeatureKey.CvAiAnalysis).collect { access ->
                    _state.update { it.copy(cvOptimizeAccess = access) }
                    if (access is FeatureAccess.StaleCacheBlocked) {
                        refreshAccess()
                    }
                }
            }
            launch {
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
    }

    private fun observeProfile() {
        if (profileObservationJob != null) return
        profileObservationJob = viewModelScope.launch {
            observeCurrentProfile().collect { profile ->
                _state.update {
                    it.copy(
                        trackId = profile.career.trackId,
                        trackName = profile.career.trackName,
                    )
                }
            }
        }
    }

    private fun loadScore(workspaceId: Long) {
        if (this.workspaceId == workspaceId && _state.value.score != null) return
        this.workspaceId = workspaceId
        if (activeOperation?.isActive == true) return

        savedStateHandle[attemptKey(workspaceId)] = true
        activeOperation = viewModelScope.launch {
            val access = checkFeatureAccess(FeatureKey.AtsFeatures).first()
            var handled = false
            access.handle(
                onGranted = { /* proceed — handled below */ },
                onLocked = { locked ->
                    handled = true
                    savedStateHandle[attemptKey(workspaceId)] = false
                    _state.update {
                        it.copy(
                            showGateSheet = true,
                            gatePlanFeatures = PlanAccessMap.featuresFor(locked.requiredPlan).map { f -> f.displayName() },
                            gateRequiredPlan = locked.requiredPlan,
                            gateFeatureName = FeatureKey.AtsFeatures.displayName(),
                            isLoading = false,
                        )
                    }
                },
                onCoinTopUpRequired = { coinReq ->
                    handled = true
                    savedStateHandle[attemptKey(workspaceId)] = false
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
                    savedStateHandle[attemptKey(workspaceId)] = false
                    refreshAccess()
                    _state.update { it.copy(isLoading = false) }
                },
            )
            if (handled) return@launch

            _state.update {
                it.copy(
                    isLoading = true,
                    error = null,
                    hasInsufficientCoins = false,
                )
            }
            val workspaceResult = getWorkspace(workspaceId)
            if (workspaceResult is CareerPilotResult.Error) {
                savedStateHandle[attemptKey(workspaceId)] = false
                _state.update {
                    it.copy(isLoading = false, error = workspaceResult.error.toUIText())
                }
                return@launch
            }
            val workspace = (workspaceResult as CareerPilotResult.Success).data
            _state.update { it.copy(workspace = workspace) }
            when (val result = scoreCv(workspaceId)) {
                is CareerPilotResult.Success -> {
                    savedStateHandle[attemptKey(workspaceId)] = false
                    _state.update {
                        it.copy(
                            isLoading = false,
                            score = result.data,
                            wasInterrupted = false,
                        )
                    }
                }
                is CareerPilotResult.Error -> {
                    val interrupted = result.error == NetworkError.TIME_OUT
                    if (!interrupted) savedStateHandle[attemptKey(workspaceId)] = false
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

    fun onIntent(intent: ScoringIntent) {
        when (intent) {
            is ScoringIntent.Initial -> {
                observeProfile()
                loadScore(intent.workspaceId)
            }
            ScoringIntent.Retry -> workspaceId?.let {
                _state.update { state -> state.copy(score = null) }
                loadScore(it)
            }
            ScoringIntent.OpenCoins -> emit(ScoringEffect.OpenCoinsPaywall)
            ScoringIntent.GenerateCoverLetter -> workspaceId?.let {
                emit(ScoringEffect.OpenCoverLetter(it))
            }
            ScoringIntent.OptimizeCv -> startOptimization()
            ScoringIntent.StartPractice -> _state.value.trackId?.let { trackId ->
                workspaceId?.let { workspaceId ->
                    emit(
                        ScoringEffect.OpenPractice(
                            trackId = trackId,
                            trackName = _state.value.trackName,
                            workspaceId = workspaceId,
                        ),
                    )
                }
            }
            ScoringIntent.DismissGateSheet -> _state.update { it.copy(showGateSheet = false) }
            ScoringIntent.DismissCoinTopUpSheet -> _state.update { it.copy(showCoinTopUpSheet = false) }
            ScoringIntent.UpgradeFromGate -> {
                _state.update { it.copy(showGateSheet = false) }
                emit(ScoringEffect.OpenCoinsPaywall)
            }
            ScoringIntent.BuyCoinsClicked -> {
                _state.update { it.copy(showCoinTopUpSheet = false) }
                emit(ScoringEffect.OpenCoinsPaywall)
            }
        }
    }

    private fun emit(effect: ScoringEffect) {
        viewModelScope.launch { effectChannel.send(effect) }
    }

    private fun startOptimization() {
        val id = workspaceId ?: return
        if (optimizationOperation?.isActive == true) return

        optimizationOperation = viewModelScope.launch {
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
                            isStartingOptimization = false,
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
                            isStartingOptimization = false,
                        )
                    }
                },
                onStale = {
                    handled = true
                    refreshAccess()
                    _state.update { it.copy(isStartingOptimization = false) }
                },
            )
            if (handled) return@launch

            _state.update {
                it.copy(
                    isStartingOptimization = true,
                    optimizationError = null,
                    hasInsufficientCoins = false,
                )
            }
            when (val result = optimizeCv(id)) {
                is CareerPilotResult.Success -> {
                    _state.update { it.copy(isStartingOptimization = false) }
                    effectChannel.send(ScoringEffect.StartOptimizationTracking(result.data))
                    effectChannel.send(
                        ScoringEffect.ShowMessage(
                            UIText.StringResource(R.string.ats_cv_optimization_queued),
                        ),
                    )
                }
                is CareerPilotResult.Error -> _state.update {
                    it.copy(
                        isStartingOptimization = false,
                        optimizationError = result.error.toUIText(),
                        hasInsufficientCoins = result.error == NetworkError.INSUFFICIENT_COINS,
                    )
                }
            }
        }
    }

    private fun attemptKey(workspaceId: Long) = "ats_score_attempted_$workspaceId"
}
