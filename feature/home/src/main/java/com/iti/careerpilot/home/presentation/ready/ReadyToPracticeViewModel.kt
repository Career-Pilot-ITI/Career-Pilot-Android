package com.iti.careerpilot.home.presentation.ready

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iti.careerpilot.core.access.PlanAccessMap
import com.iti.careerpilot.core.access.domain.AccessRepository
import com.iti.careerpilot.core.access.domain.usecase.CheckFeatureAccessUseCase
import com.iti.careerpilot.core.access.domain.usecase.RefreshAccessUseCase
import com.iti.careerpilot.home.domain.usecase.GetUserProfileUseCase
import com.iti.core.model.FeatureAccess
import com.iti.core.model.FeatureKey
import com.iti.core.model.Plan
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReadyToPracticeViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val checkFeatureAccess: CheckFeatureAccessUseCase,
    private val refreshAccess: RefreshAccessUseCase,
    private val accessRepository: AccessRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ReadyToPracticeState())
    val state = _state.asStateFlow()

    private val _events = Channel<ReadyToPracticeEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private val trackId: Long?
        get() = savedStateHandle[KEY_TRACK_ID]
    private val workspaceId: Long?
        get() = savedStateHandle[KEY_WORKSPACE_ID]

    init {
        observeAccess()
    }

    private fun initialize(trackId: Long, trackName: String, workspaceId: Long?) {
        if (!savedStateHandle.contains(KEY_TRACK_ID)) {
            savedStateHandle[KEY_TRACK_ID] = trackId
            savedStateHandle[KEY_TRACK_NAME] = trackName
            workspaceId?.let { savedStateHandle[KEY_WORKSPACE_ID] = it }
        }
        _state.update {
            it.copy(trackName = savedStateHandle.get<String>(KEY_TRACK_NAME).orEmpty())
        }
    }

    private fun observeAccess() {
        viewModelScope.launch {
            checkFeatureAccess(FeatureKey.VideoInterview).collect { access ->
                _state.update {
                    it.copy(
                        videoInterviewAccess = access,
                        videoGatePlanFeatures = if (access is FeatureAccess.Locked) {
                            PlanAccessMap.featuresFor(access.requiredPlan).map { f -> f.displayName() }
                        } else it.videoGatePlanFeatures,
                        videoGateRequiredPlan = if (access is FeatureAccess.Locked) access.requiredPlan else it.videoGateRequiredPlan
                    )
                }
                if (access is FeatureAccess.StaleCacheBlocked) {
                    refreshAccess()
                }
            }
        }

        viewModelScope.launch {
            checkFeatureAccess(FeatureKey.MockInterviews).collect { access ->
                _state.update { it.copy(audioInterviewAccess = access) }
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
                        isPaidPlan = accessState.plan != Plan.FREE
                    )
                }
            }
        }
    }

    fun initialise(trackId: Long, trackName: String) {
        if (savedStateHandle.contains(KEY_TRACK_ID)) return
        savedStateHandle[KEY_TRACK_ID] = trackId
        savedStateHandle[KEY_TRACK_NAME] = trackName
        _state.update { it.copy(trackName = trackName) }
    }

    fun onAction(action: ReadyToPracticeAction) {
        when (action) {
            is ReadyToPracticeAction.Initial -> initialize(
                action.trackId,
                action.trackName,
                action.workspaceId,
            )
            is ReadyToPracticeAction.MicrophonePermissionChanged -> _state.update {
                it.copy(
                    isMicrophoneGranted = action.isGranted,
                    isPermissionDialogVisible = false,
                )
            }

            is ReadyToPracticeAction.CameraPermissionChanged -> _state.update {
                it.copy(
                    isCameraGranted = action.isGranted,
                    showCameraPermissionDialog = false,
                )
            }

            ReadyToPracticeAction.SelectAudioMode -> _state.update {
                it.copy(
                    isVideoMode = false,
                    enablePostureTracking = false,
                    enableHandTracking = false
                )
            }

            is ReadyToPracticeAction.TogglePostureTracking -> _state.update {
                it.copy(enablePostureTracking = action.enabled)
            }

            is ReadyToPracticeAction.ToggleHandTracking -> _state.update {
                it.copy(enableHandTracking = action.enabled)
            }

            ReadyToPracticeAction.SelectVideoMode -> {
                when (val access = _state.value.videoInterviewAccess) {
                    is FeatureAccess.Granted -> {
                        _state.update {
                            it.copy(
                                isVideoMode = true,
                                showCameraPermissionDialog = !it.isCameraGranted
                            )
                        }
                    }
                    is FeatureAccess.Locked -> {
                        _state.update {
                            it.copy(
                                showVideoGateSheet = true,
                                videoGatePlanFeatures = PlanAccessMap.featuresFor(access.requiredPlan).map { f -> f.displayName() },
                                videoGateRequiredPlan = access.requiredPlan
                            )
                        }
                    }
                    is FeatureAccess.CoinTopUpRequired -> {
                        _state.update {
                            it.copy(
                                showCoinTopUpSheet = true,
                                coinTopUpRequiredCost = access.coinCost
                            )
                        }
                    }
                    is FeatureAccess.StaleCacheBlocked -> {
                        viewModelScope.launch { refreshAccess() }
                    }
                    is FeatureAccess.Unknown -> {
                        val cost = _state.value.videoCoinCost
                        if (_state.value.coinBalance < cost) {
                            _state.update {
                                it.copy(
                                    showCoinTopUpSheet = true,
                                    coinTopUpRequiredCost = cost
                                )
                            }
                        } else {
                            _state.update {
                                it.copy(
                                    isVideoMode = true,
                                    showCameraPermissionDialog = !it.isCameraGranted
                                )
                            }
                        }
                    }
                }
            }

            ReadyToPracticeAction.MicrophoneRowClicked -> _state.update {
                if (it.isMicrophoneGranted) it else it.copy(isPermissionDialogVisible = true)
            }

            ReadyToPracticeAction.PermissionDialogDismissed -> _state.update {
                it.copy(isPermissionDialogVisible = false)
            }

            ReadyToPracticeAction.CameraRowClicked -> _state.update {
                if (it.isCameraGranted) it else it.copy(showCameraPermissionDialog = true)
            }

            ReadyToPracticeAction.CameraPermissionDialogDismissed -> _state.update {
                it.copy(showCameraPermissionDialog = false)
            }

            ReadyToPracticeAction.BeginInterviewClicked,
            ReadyToPracticeAction.StartPracticeClicked -> beginInterview()

            ReadyToPracticeAction.CancelClicked -> sendEvent(ReadyToPracticeEvent.NavigateBack)

            ReadyToPracticeAction.DismissVideoGateSheet -> _state.update { it.copy(showVideoGateSheet = false) }

            ReadyToPracticeAction.DismissCoinTopUpSheet -> _state.update { it.copy(showCoinTopUpSheet = false) }

            ReadyToPracticeAction.UpgradeFromVideoGate -> {
                _state.update { it.copy(showVideoGateSheet = false) }
                sendEvent(ReadyToPracticeEvent.NavigateToPaywall)
            }

            ReadyToPracticeAction.BuyCoinsClicked -> {
                _state.update { it.copy(showCoinTopUpSheet = false) }
                sendEvent(ReadyToPracticeEvent.NavigateToPaywall)
            }
        }
    }

    private fun beginInterview() {
        val id = trackId ?: return
        if (!_state.value.canBegin) return
        val s = _state.value

        val currentAccess = if (s.isVideoMode) s.videoInterviewAccess else s.audioInterviewAccess
        when (currentAccess) {
            is FeatureAccess.Locked -> {
                if (s.isVideoMode) {
                    _state.update { it.copy(showVideoGateSheet = true) }
                } else {
                    sendEvent(ReadyToPracticeEvent.NavigateToPaywall)
                }
                return
            }
            is FeatureAccess.CoinTopUpRequired -> {
                _state.update {
                    it.copy(
                        showCoinTopUpSheet = true,
                        coinTopUpRequiredCost = currentAccess.coinCost
                    )
                }
                return
            }
            is FeatureAccess.StaleCacheBlocked -> {
                viewModelScope.launch { refreshAccess() }
                return
            }
            is FeatureAccess.Unknown -> {
                val cost = if (s.isVideoMode) s.videoCoinCost else s.voiceCoinCost
                if (s.coinBalance < cost) {
                    _state.update {
                        it.copy(
                            showCoinTopUpSheet = true,
                            coinTopUpRequiredCost = cost
                        )
                    }
                    return
                }
            }
            is FeatureAccess.Granted -> Unit
        }

        sendEvent(
            ReadyToPracticeEvent.NavigateToPractice(
                trackId = id,
                workspaceId = workspaceId,
                isVideo = s.isVideoMode,
                enablePosture = s.enablePostureTracking,
                enableHands = s.enableHandTracking,
            )
        )
    }

    private fun sendEvent(event: ReadyToPracticeEvent) {
        viewModelScope.launch { _events.send(event) }
    }

    private companion object {
        const val KEY_TRACK_ID = "ready_track_id"
        const val KEY_TRACK_NAME = "ready_track_name"
        const val KEY_WORKSPACE_ID = "ready_workspace_id"
    }
}
