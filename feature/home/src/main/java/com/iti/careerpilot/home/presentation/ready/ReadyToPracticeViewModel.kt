package com.iti.careerpilot.home.presentation.ready

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iti.careerpilot.core.access.PlanAccessMap
import com.iti.careerpilot.core.access.domain.AccessRepository
import com.iti.careerpilot.core.access.domain.usecase.CheckFeatureAccessUseCase
import com.iti.careerpilot.core.access.domain.usecase.RefreshAccessUseCase
import com.iti.careerpilot.core.access.domain.usecase.handle
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

    private val _events = Channel<ReadyToPracticeEffect>(Channel.BUFFERED)
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

    fun onIntent(intent: ReadyToPracticeIntent) {
        when (intent) {
            is ReadyToPracticeIntent.Initial -> initialize(
                intent.trackId,
                intent.trackName,
                intent.workspaceId,
            )
            is ReadyToPracticeIntent.MicrophonePermissionChanged -> _state.update {
                it.copy(
                    isMicrophoneGranted = intent.isGranted,
                    isPermissionDialogVisible = false,
                )
            }

            is ReadyToPracticeIntent.CameraPermissionChanged -> _state.update {
                it.copy(
                    isCameraGranted = intent.isGranted,
                    showCameraPermissionDialog = false,
                )
            }

            ReadyToPracticeIntent.SelectAudioMode -> _state.update {
                it.copy(
                    isVideoMode = false,
                    enablePostureTracking = false,
                    enableHandTracking = false
                )
            }

            is ReadyToPracticeIntent.TogglePostureTracking -> _state.update {
                it.copy(enablePostureTracking = intent.enabled)
            }

            is ReadyToPracticeIntent.ToggleHandTracking -> _state.update {
                it.copy(enableHandTracking = intent.enabled)
            }

            ReadyToPracticeIntent.SelectVideoMode -> {
                _state.value.videoInterviewAccess.handle(
                    onGranted = {
                        _state.update {
                            it.copy(
                                isVideoMode = true,
                                showCameraPermissionDialog = !it.isCameraGranted
                            )
                        }
                    },
                    onLocked = { locked ->
                        _state.update {
                            it.copy(
                                showVideoGateSheet = true,
                                videoGatePlanFeatures = PlanAccessMap.featuresFor(locked.requiredPlan).map { f -> f.displayName() },
                                videoGateRequiredPlan = locked.requiredPlan
                            )
                        }
                    },
                    onCoinTopUpRequired = { coinReq ->
                        _state.update {
                            it.copy(
                                showCoinTopUpSheet = true,
                                coinTopUpRequiredCost = coinReq.coinCost
                            )
                        }
                    },
                    onStale = {
                        viewModelScope.launch { refreshAccess() }
                    },
                    onUnknown = {
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
                    },
                )
            }

            ReadyToPracticeIntent.MicrophoneRowClicked -> _state.update {
                if (it.isMicrophoneGranted) it else it.copy(isPermissionDialogVisible = true)
            }

            ReadyToPracticeIntent.PermissionDialogDismissed -> _state.update {
                it.copy(isPermissionDialogVisible = false)
            }

            ReadyToPracticeIntent.CameraRowClicked -> _state.update {
                if (it.isCameraGranted) it else it.copy(showCameraPermissionDialog = true)
            }

            ReadyToPracticeIntent.CameraPermissionDialogDismissed -> _state.update {
                it.copy(showCameraPermissionDialog = false)
            }

            ReadyToPracticeIntent.BeginInterviewClicked,
            ReadyToPracticeIntent.StartPracticeClicked -> beginInterview()

            ReadyToPracticeIntent.CancelClicked -> sendEvent(ReadyToPracticeEffect.NavigateBack)

            ReadyToPracticeIntent.DismissVideoGateSheet -> _state.update { it.copy(showVideoGateSheet = false) }

            ReadyToPracticeIntent.DismissCoinTopUpSheet -> _state.update { it.copy(showCoinTopUpSheet = false) }

            ReadyToPracticeIntent.UpgradeFromVideoGate -> {
                _state.update { it.copy(showVideoGateSheet = false) }
                sendEvent(ReadyToPracticeEffect.NavigateToPaywall)
            }

            ReadyToPracticeIntent.BuyCoinsClicked -> {
                _state.update { it.copy(showCoinTopUpSheet = false) }
                sendEvent(ReadyToPracticeEffect.NavigateToPaywall)
            }
        }
    }

    private fun beginInterview() {
        val id = trackId ?: return
        if (!_state.value.canBegin) return
        val s = _state.value

        val currentAccess = if (s.isVideoMode) s.videoInterviewAccess else s.audioInterviewAccess
        var handled = false
        currentAccess.handle(
            onGranted = { /* proceed below */ },
            onLocked = {
                handled = true
                if (s.isVideoMode) {
                    _state.update { it.copy(showVideoGateSheet = true) }
                } else {
                    sendEvent(ReadyToPracticeEffect.NavigateToPaywall)
                }
            },
            onCoinTopUpRequired = { coinReq ->
                handled = true
                _state.update {
                    it.copy(
                        showCoinTopUpSheet = true,
                        coinTopUpRequiredCost = coinReq.coinCost
                    )
                }
            },
            onStale = {
                handled = true
                viewModelScope.launch { refreshAccess() }
            },
            onUnknown = {
                val cost = if (s.isVideoMode) s.videoCoinCost else s.voiceCoinCost
                if (s.coinBalance < cost) {
                    handled = true
                    _state.update {
                        it.copy(
                            showCoinTopUpSheet = true,
                            coinTopUpRequiredCost = cost
                        )
                    }
                }
            },
        )
        if (handled) return

        sendEvent(
            ReadyToPracticeEffect.NavigateToPractice(
                trackId = id,
                workspaceId = workspaceId,
                isVideo = s.isVideoMode,
                enablePosture = s.enablePostureTracking,
                enableHands = s.enableHandTracking,
            )
        )
    }

    private fun sendEvent(event: ReadyToPracticeEffect) {
        viewModelScope.launch { _events.send(event) }
    }

    private companion object {
        const val KEY_TRACK_ID = "ready_track_id"
        const val KEY_TRACK_NAME = "ready_track_name"
        const val KEY_WORKSPACE_ID = "ready_workspace_id"
    }
}
