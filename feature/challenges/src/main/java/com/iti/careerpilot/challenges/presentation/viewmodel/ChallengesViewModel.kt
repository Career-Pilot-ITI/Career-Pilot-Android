package com.iti.careerpilot.challenges.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iti.careerpilot.challenges.domain.repository.ChallengesRepository
import com.iti.careerpilot.challenges.presentation.action.ChallengesAction
import com.iti.careerpilot.challenges.presentation.event.ChallengesEvent
import com.iti.careerpilot.challenges.presentation.state.ChallengesState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChallengesViewModel @Inject constructor(
    private val repository: ChallengesRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ChallengesState())
    val state = _state.asStateFlow()

    private val _events = Channel<ChallengesEvent>()
    val events = _events.receiveAsFlow()

    fun onAction(action: ChallengesAction) {
        when (action) {
            ChallengesAction.CreateChallengeClicked -> {
                viewModelScope.launch {
                    _events.send(ChallengesEvent.NavigateToCreateChallenge)
                }
            }
            ChallengesAction.ChallengeDashboardClicked -> {
                viewModelScope.launch {
                    _events.send(ChallengesEvent.NavigateToChallengeDashboard)
                }
            }
        }
    }
}
