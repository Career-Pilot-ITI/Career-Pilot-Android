package com.iti.careerpilot.profile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iti.careerpilot.profile.domain.repo.ProfileRepo
import com.iti.careerpilot.profile.presentation.action.ProfileIntent
import com.iti.careerpilot.profile.presentation.event.ProfileEffect
import com.iti.careerpilot.profile.presentation.state.ProfileState
import com.iti.careerpilot.core.network.auth.SessionManager
import com.iti.common.dispatcher.CareerPilotDispatchers
import com.iti.common.dispatcher.Dispatcher
import com.iti.common.result.onError
import com.iti.common.snackbar.CareerPilotSnackbarController
import com.iti.common.util.toUIText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepo: ProfileRepo,
    private val sessionManager: SessionManager,
    @param:Dispatcher(CareerPilotDispatchers.Default) private val dispatcherDefault: CoroutineDispatcher,
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())
    val state = _state
        .combine(profileRepo.userProfile) { state, profile ->
            state.copy(profile = profile)
        }
        .flowOn(dispatcherDefault)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = ProfileState()
        )

    private val _events = Channel<ProfileEffect>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private var initializationJob: Job? = null

    private fun initialize() {
        if (initializationJob != null) return
        initializationJob = viewModelScope.launch {
            profileRepo.downloadMissingFiles()
            if (profileRepo.userProfile.value.id == 0L) {
                refreshProfile()
            }
        }
    }

    fun onIntent(intent: ProfileIntent) {
        when (intent) {
            ProfileIntent.Initial -> initialize()
            is ProfileIntent.OnEditProfileClick -> sendEvent(ProfileEffect.NavigateToEditProfile(intent.section))
            ProfileIntent.OnSettingsClick -> sendEvent(ProfileEffect.NavigateToSettings)
            ProfileIntent.OnSubscriptionClick -> sendEvent(ProfileEffect.NavigateToSubscription)

            ProfileIntent.OnLogoutClick -> _state.update { it.copy(showLogoutDialog = true) }
            ProfileIntent.OnLogoutDismiss -> _state.update { it.copy(showLogoutDialog = false) }
            ProfileIntent.OnLogoutConfirm -> {
                _state.update { it.copy(showLogoutDialog = false) }
                viewModelScope.launch {
                    sessionManager.clearSession()
                    profileRepo.clearUserProfile()
                    sendEvent(ProfileEffect.NavigateToLogout)
                }
            }

            is ProfileIntent.OnCVClick -> sendEvent(ProfileEffect.OpenCV(intent.cvLocalUriOrUrl))
        }
    }

    private fun refreshProfile() {
        viewModelScope.launch {
            profileRepo.refreshProfile()
                .onError { error ->
                    CareerPilotSnackbarController.show(error.toUIText())
                }
        }
    }

    private fun sendEvent(event: ProfileEffect) {
        viewModelScope.launch(dispatcherDefault) { _events.send(event) }
    }
}
