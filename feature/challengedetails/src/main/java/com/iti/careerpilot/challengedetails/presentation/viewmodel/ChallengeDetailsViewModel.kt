package com.iti.careerpilot.challengedetails.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iti.careerpilot.challengedetails.domain.repository.ChallengeDetailsRepository
import com.iti.careerpilot.challengedetails.presentation.action.ChallengeDetailsAction
import com.iti.careerpilot.challengedetails.presentation.event.ChallengeDetailsEvent
import com.iti.careerpilot.challengedetails.presentation.state.ChallengeDetailsState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChallengeDetailsViewModel @Inject constructor(
    private val repository: ChallengeDetailsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ChallengeDetailsState())
    val state = _state.asStateFlow()

    private val _events = Channel<ChallengeDetailsEvent>()
    val events = _events.receiveAsFlow()

    fun onAction(action: ChallengeDetailsAction) {
        when (action) {
            ChallengeDetailsAction.OnBackClicked -> {
                viewModelScope.launch {
                    _events.send(ChallengeDetailsEvent.NavigateBack)
                }
            }
        }
    }
}
