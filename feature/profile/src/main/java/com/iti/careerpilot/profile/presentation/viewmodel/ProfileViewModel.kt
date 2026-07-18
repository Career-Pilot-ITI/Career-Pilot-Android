package com.iti.careerpilot.profile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iti.careerpilot.profile.domain.repo.ProfileRepo
import com.iti.careerpilot.profile.presentation.action.ProfileAction
import com.iti.careerpilot.profile.presentation.event.ProfileEvent
import com.iti.careerpilot.profile.presentation.state.ProfileState
import com.iti.common.dispatcher.CareerPilotDispatchers
import com.iti.common.dispatcher.Dispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
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
    profileRepo: ProfileRepo,
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

    private val _events = Channel<ProfileEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun onAction(action: ProfileAction) {
        when (action) {
            is ProfileAction.OnEditProfileClick -> sendEvent(ProfileEvent.NavigateToEditProfile(action.section))
            ProfileAction.OnSettingsClick -> sendEvent(ProfileEvent.NavigateToSettings)

            ProfileAction.OnLogoutClick -> _state.update { it.copy(showLogoutDialog = true) }
            ProfileAction.OnLogoutDismiss -> _state.update { it.copy(showLogoutDialog = false) }
            ProfileAction.OnLogoutConfirm -> {
                _state.update { it.copy(showLogoutDialog = false) }
                sendEvent(ProfileEvent.NavigateToLogout)
            }
        }
    }

    private fun sendEvent(event: ProfileEvent) {
        viewModelScope.launch(dispatcherDefault) { _events.send(event) }
    }
}