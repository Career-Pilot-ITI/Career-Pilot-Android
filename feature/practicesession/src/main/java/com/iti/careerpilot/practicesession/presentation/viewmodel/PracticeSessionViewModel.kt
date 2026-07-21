package com.iti.careerpilot.practicesession.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iti.careerpilot.practicesession.domain.repo.SessionRepo
import com.iti.careerpilot.practicesession.presentation.action.PracticeSessionAction
import com.iti.careerpilot.practicesession.presentation.state.PracticeSessionState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class PracticeSessionViewModel @Inject constructor(
    private val sessionRepo: SessionRepo
) : ViewModel() {

    private var hasLoadedInitialData = false

    private val _state = MutableStateFlow(PracticeSessionState())
    val state = _state
        .onStart {
            if (!hasLoadedInitialData) {
                /** Load initial data here **/
                hasLoadedInitialData = true
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = PracticeSessionState()
        )

    fun onAction(action: PracticeSessionAction) {
        when (action) {
            is PracticeSessionAction.StartPracticeSession -> {
               _state.update {
                   it.copy(
                       sessionId = action.sessionId
                   )
               }
            }
        }
    }

}