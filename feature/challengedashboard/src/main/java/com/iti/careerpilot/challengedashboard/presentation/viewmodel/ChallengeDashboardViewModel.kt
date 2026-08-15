package com.iti.careerpilot.challengedashboard.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iti.careerpilot.challengedashboard.domain.repository.ChallengeDashboardRepository
import com.iti.careerpilot.challengedashboard.presentation.action.ChallengeDashboardAction
import com.iti.careerpilot.challengedashboard.presentation.event.ChallengeDashboardEvent
import com.iti.careerpilot.challengedashboard.presentation.state.ChallengeDashboardState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChallengeDashboardViewModel @Inject constructor(
    private val repository: ChallengeDashboardRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ChallengeDashboardState())
    val state = _state.asStateFlow()

    private val _events = Channel<ChallengeDashboardEvent>()
    val events = _events.receiveAsFlow()

    fun onAction(action: ChallengeDashboardAction) {
        when (action) {
            ChallengeDashboardAction.OnBackClicked -> {
                viewModelScope.launch {
                    _events.send(ChallengeDashboardEvent.NavigateBack)
                }
            }
        }
    }
}
