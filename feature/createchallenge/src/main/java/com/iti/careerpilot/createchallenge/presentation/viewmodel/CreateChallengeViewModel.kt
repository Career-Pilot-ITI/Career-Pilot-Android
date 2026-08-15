package com.iti.careerpilot.createchallenge.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iti.careerpilot.createchallenge.domain.repository.CreateChallengeRepository
import com.iti.careerpilot.createchallenge.presentation.action.CreateChallengeAction
import com.iti.careerpilot.createchallenge.presentation.event.CreateChallengeEvent
import com.iti.careerpilot.createchallenge.presentation.state.CreateChallengeState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateChallengeViewModel @Inject constructor(
    private val repository: CreateChallengeRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CreateChallengeState())
    val state = _state.asStateFlow()

    private val _events = Channel<CreateChallengeEvent>()
    val events = _events.receiveAsFlow()

    fun onAction(action: CreateChallengeAction) {
        when (action) {
            CreateChallengeAction.OnBackClicked -> {
                viewModelScope.launch {
                    _events.send(CreateChallengeEvent.NavigateBack)
                }
            }
        }
    }
}
