package com.iti.careerpilot.ats.presentation.coverletter.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iti.careerpilot.ats.R
import com.iti.careerpilot.ats.domain.usecase.GenerateCoverLetterUseCase
import com.iti.careerpilot.ats.domain.usecase.GetWorkspaceUseCase
import com.iti.careerpilot.ats.domain.usecase.ObserveCurrentProfileUseCase
import com.iti.careerpilot.ats.presentation.util.coverLetterEmailDraft
import com.iti.careerpilot.ats.presentation.coverletter.state.CoverLetterAction
import com.iti.careerpilot.ats.presentation.coverletter.state.CoverLetterEffect
import com.iti.careerpilot.ats.presentation.coverletter.state.CoverLetterUiState
import com.iti.careerpilot.core.access.PlanAccessMap
import com.iti.careerpilot.core.access.domain.AccessRepository
import com.iti.careerpilot.core.access.domain.usecase.CheckFeatureAccessUseCase
import com.iti.careerpilot.core.access.domain.usecase.RefreshAccessUseCase
import com.iti.careerpilot.core.access.domain.usecase.handle
import com.iti.common.error.NetworkError
import com.iti.common.result.CareerPilotResult
import com.iti.common.util.UIText
import com.iti.common.util.toUIText
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
class CoverLetterViewModel @Inject constructor(
    private val getWorkspace: GetWorkspaceUseCase,
    private val generateCoverLetter: GenerateCoverLetterUseCase,
    private val observeCurrentProfile: ObserveCurrentProfileUseCase,
    private val savedStateHandle: SavedStateHandle,
    private val checkFeatureAccess: CheckFeatureAccessUseCase,
    private val refreshAccess: RefreshAccessUseCase,
    private val accessRepository: AccessRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(CoverLetterUiState())
    val state = _state.asStateFlow()
    private val effectChannel = Channel<CoverLetterEffect>(Channel.BUFFERED)
    val effects = effectChannel.receiveAsFlow()
    private var workspaceId: Long? = null
    private var activeOperation: Job? = null
    private var profileObservationJob: Job? = null
    private var accessObservationJob: Job? = null

    init {
        observeAccess()
    }

    private fun observeAccess() {
        if (accessObservationJob != null) return
        accessObservationJob = viewModelScope.launch {
            launch {
                checkFeatureAccess(FeatureKey.CoverLetter).collect { access ->
                    _state.update {
                        it.copy(
                            coverLetterAccess = access,
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
                        contactName = profile.personal.displayName,
                        contactEmail = profile.account.email,
                        contactPhone = profile.personal.phoneNumber,
                    )
                }
            }
        }
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
                    val restored = result.data.coverLetterText.orEmpty()
                    val wasInterrupted = savedStateHandle.get<Boolean>(attemptKey(workspaceId)) == true
                    it.copy(
                        workspace = result.data,
                        generatedValue = restored,
                        editedValue = restored,
                        isLoading = false,
                        wasInterrupted = wasInterrupted,
                    )
                }.also {
                    if (_state.value.editedValue.isBlank() && !_state.value.wasInterrupted) {
                        executeGeneration()
                    }
                }
            }
        }
    }

    fun onAction(action: CoverLetterAction) {
        when (action) {
            is CoverLetterAction.Initial -> {
                observeProfile()
                loadWorkspace(action.workspaceId)
            }
            CoverLetterAction.Retry -> if (_state.value.workspace == null) {
                workspaceId?.let(::loadWorkspace)
            } else {
                executeGeneration()
            }
            CoverLetterAction.ToggleEditing -> _state.update { it.copy(isEditing = !it.isEditing) }
            is CoverLetterAction.EditedValueChanged -> _state.update { it.copy(editedValue = action.value) }
            CoverLetterAction.Copy -> _state.value.editedValue.takeIf(String::isNotBlank)?.let {
                emit(CoverLetterEffect.CopyText(it))
            }
            CoverLetterAction.Email -> _state.value.editedValue.takeIf(String::isNotBlank)?.let { body ->
                emit(
                    CoverLetterEffect.ComposeEmail(
                        coverLetterEmailDraft(
                            workspace = _state.value.workspace,
                            body = body,
                            genericSubject = UIText.StringResource(R.string.ats_cover_letter_email_subject),
                        ),
                    ),
                )
            }
            CoverLetterAction.OpenCoins -> emit(CoverLetterEffect.OpenCoinsPaywall)
            CoverLetterAction.DismissGateSheet -> _state.update { it.copy(showGateSheet = false) }
            CoverLetterAction.DismissCoinTopUpSheet -> _state.update { it.copy(showCoinTopUpSheet = false) }
            CoverLetterAction.UpgradeFromGate -> {
                _state.update { it.copy(showGateSheet = false) }
                emit(CoverLetterEffect.OpenCoinsPaywall)
            }
            CoverLetterAction.BuyCoinsClicked -> {
                _state.update { it.copy(showCoinTopUpSheet = false) }
                emit(CoverLetterEffect.OpenCoinsPaywall)
            }
        }
    }

    private fun executeGeneration() {
        val id = workspaceId ?: return
        if (_state.value.isLoading || activeOperation?.isActive == true) return

        savedStateHandle[attemptKey(id)] = true
        activeOperation = viewModelScope.launch {
            val access = checkFeatureAccess(FeatureKey.CoverLetter).first()
            var handled = false
            access.handle(
                onGranted = { /* proceed — handled below */ },
                onLocked = { locked ->
                    handled = true
                    savedStateHandle[attemptKey(id)] = false
                    _state.update {
                        it.copy(
                            showGateSheet = true,
                            gatePlanFeatures = PlanAccessMap.featuresFor(locked.requiredPlan).map { f -> f.displayName() },
                            gateRequiredPlan = locked.requiredPlan,
                            isLoading = false,
                        )
                    }
                },
                onCoinTopUpRequired = { coinReq ->
                    handled = true
                    savedStateHandle[attemptKey(id)] = false
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
                    savedStateHandle[attemptKey(id)] = false
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
            when (val result = generateCoverLetter(id)) {
                is CareerPilotResult.Success -> {
                    savedStateHandle[attemptKey(id)] = false
                    _state.update {
                        it.copy(
                            generatedValue = result.data.body,
                            editedValue = result.data.body,
                            approachTips = result.data.approachTips,
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

    private fun emit(effect: CoverLetterEffect) {
        viewModelScope.launch { effectChannel.send(effect) }
    }

    private fun attemptKey(id: Long) = "ats_cover_letter_attempted_$id"
}
