package com.iti.careerpilot.profile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iti.careerpilot.profile.domain.models.RequestProfileUpdate
import com.iti.careerpilot.profile.domain.repo.ProfileRepo
import com.iti.careerpilot.profile.presentation.action.ProfileAction
import com.iti.careerpilot.profile.presentation.state.ProfileState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepo: ProfileRepo
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())
    val state = _state
        .combine(profileRepo.userProfile) {
            state, profile -> state.copy(profile = profile)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = ProfileState()
        )

    fun onAction(action: ProfileAction) {
        when (action) {
            else -> TODO("Handle actions")
        }
    }

}