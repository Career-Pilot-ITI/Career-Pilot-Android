package com.iti.careerpilot.practicesession.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iti.careerpilot.practicesession.domain.repo.SessionRepo
import com.iti.careerpilot.practicesession.presentation.action.PracticeSessionAction
import com.iti.careerpilot.practicesession.presentation.event.PracticeSessionEvent
import com.iti.careerpilot.practicesession.presentation.state.PracticeSessionState
import com.iti.careerpilot.whisper.domain.WhisperEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class PracticeSessionViewModel @Inject constructor(
    private val sessionRepo: SessionRepo,
    private val whisperEngine: WhisperEngine
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

    private val _event = Channel<PracticeSessionEvent>()
    val event: Flow<PracticeSessionEvent> = _event.receiveAsFlow()

    fun onAction(action: PracticeSessionAction) {
        when (action) {
            is PracticeSessionAction.CreateNewPracticeSession -> TODO()
            is PracticeSessionAction.StartPracticeSession -> {
                startSession(action)
            }
            is PracticeSessionAction.ShowOrHidePermissionDialog -> {
                _state.update {
                    it.copy(
                        showPermissionDialog = action.show
                    )
                }
            }

            PracticeSessionAction.ListenToAIReadingCurrentQuestion -> TODO()
            PracticeSessionAction.PauseListeningToCurrentQuestion -> TODO()
            PracticeSessionAction.StopListeningToCurrentQuestionAndStartAnswering -> TODO()

            PracticeSessionAction.DiscardCurrentAnswerAndMakeNewOne -> TODO()
            PracticeSessionAction.FinishRecordingAnswerAndStartTranscription -> TODO()
            PracticeSessionAction.PauseRecordingAnswer -> TODO()
            PracticeSessionAction.PlayCurrentRecordedAnswer -> TODO()
            PracticeSessionAction.ResumeRecordingAnswer -> TODO()
            PracticeSessionAction.StartRecordingAnswer -> TODO()

            PracticeSessionAction.SubmitFinalAnswerToCurrentQuestion -> TODO()
        }
    }

    private fun startSession(
        action: PracticeSessionAction.StartPracticeSession
    ) {
        _state.update {
            it.copy(
                sessionId = action.sessionId,
            )
        }
    }

}