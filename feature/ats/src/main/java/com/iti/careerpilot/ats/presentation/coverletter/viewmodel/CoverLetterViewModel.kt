package com.iti.careerpilot.ats.presentation.coverletter.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iti.careerpilot.ats.R
import com.iti.careerpilot.ats.domain.usecase.GenerateCoverLetterUseCase
import com.iti.careerpilot.ats.domain.usecase.GetWorkspaceUseCase
import com.iti.careerpilot.ats.domain.usecase.ObserveCurrentProfileUseCase
import com.iti.careerpilot.ats.presentation.util.coverLetterEmailDraft
import com.iti.careerpilot.ats.presentation.coverletter.state.CoverLetterAction
import com.iti.careerpilot.ats.presentation.coverletter.state.CoverLetterEffect
import com.iti.careerpilot.ats.presentation.coverletter.state.CoverLetterUiState
import com.iti.common.error.NetworkError
import com.iti.common.result.CareerPilotResult
import com.iti.common.util.UIText
import com.iti.common.util.toUIText
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class CoverLetterViewModel @Inject constructor(
    private val getWorkspace: GetWorkspaceUseCase,
    private val generateCoverLetter: GenerateCoverLetterUseCase,
    private val observeCurrentProfile: ObserveCurrentProfileUseCase,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val _state = MutableStateFlow(CoverLetterUiState())
    val state = _state.asStateFlow()
    private val effectChannel = Channel<CoverLetterEffect>(Channel.BUFFERED)
    val effects = effectChannel.receiveAsFlow()
    private var workspaceId: Long? = null
    private var activeOperation: Job? = null
    private var profileObservationJob: Job? = null

    private fun observeProfile() {
        if (profileObservationJob != null) return
        profileObservationJob = viewModelScope.launch {
            observeCurrentProfile().collect { profile ->
                _state.update {
                    it.copy(
                        contactName = profile.personal.displayName,
                        contactEmail = profile.account.email,
                        contactPhone = profile.personal.phoneNumber,
                    )
                }
            }
        }
    }

    private fun loadWorkspace(workspaceId: Long) {
        if (this.workspaceId == workspaceId && _state.value.workspace != null) return
        this.workspaceId = workspaceId
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            when (val result = getWorkspace(workspaceId)) {
                is CareerPilotResult.Error -> _state.update {
                    it.copy(isLoading = false, error = result.error.toUIText())
                }
                is CareerPilotResult.Success -> _state.update {
                    val restored = result.data.coverLetterText.orEmpty()
                    val wasInterrupted = savedStateHandle.get<Boolean>(attemptKey(workspaceId)) == true
                    it.copy(
                        workspace = result.data,
                        generatedValue = restored,
                        editedValue = restored,
                        isLoading = false,
                        wasInterrupted = wasInterrupted,
                    )
                }.also {
                    if (_state.value.editedValue.isBlank() && !_state.value.wasInterrupted) {
                        executeGeneration()
                    }
                }
            }
        }
    }

    fun onAction(action: CoverLetterAction) {
        when (action) {
            is CoverLetterAction.Initial -> {
                observeProfile()
                loadWorkspace(action.workspaceId)
            }
            CoverLetterAction.Retry -> if (_state.value.workspace == null) {
                workspaceId?.let(::loadWorkspace)
            } else {
                executeGeneration()
            }
            CoverLetterAction.ToggleEditing -> _state.update { it.copy(isEditing = !it.isEditing) }
            is CoverLetterAction.EditedValueChanged -> _state.update { it.copy(editedValue = action.value) }
            CoverLetterAction.Copy -> _state.value.editedValue.takeIf(String::isNotBlank)?.let {
                emit(CoverLetterEffect.CopyText(it))
            }
            CoverLetterAction.Email -> _state.value.editedValue.takeIf(String::isNotBlank)?.let { body ->
                emit(
                    CoverLetterEffect.ComposeEmail(
                        coverLetterEmailDraft(
                            workspace = _state.value.workspace,
                            body = body,
                            genericSubject = UIText.StringResource(R.string.ats_cover_letter_email_subject),
                        ),
                    ),
                )
            }
            CoverLetterAction.OpenCoins -> emit(CoverLetterEffect.OpenCoinsPaywall)
        }
    }

    private fun executeGeneration() {
        val id = workspaceId ?: return
        if (_state.value.isLoading || activeOperation?.isActive == true) return
        savedStateHandle[attemptKey(id)] = true
        activeOperation = viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = true,
                    error = null,
                    hasInsufficientCoins = false,
                )
            }
            when (val result = generateCoverLetter(id)) {
                is CareerPilotResult.Success -> {
                    savedStateHandle[attemptKey(id)] = false
                    _state.update {
                        it.copy(
                            generatedValue = result.data.body,
                            editedValue = result.data.body,
                            approachTips = result.data.approachTips,
                            coinCost = result.data.coinCost,
                            isLoading = false,
                            wasInterrupted = false,
                        )
                    }
                }
                is CareerPilotResult.Error -> {
                    val interrupted = result.error == NetworkError.TIME_OUT
                    if (!interrupted) savedStateHandle[attemptKey(id)] = false
                    _state.update {
                        it.copy(
                            isLoading = false,
                            wasInterrupted = interrupted,
                            hasInsufficientCoins = result.error == NetworkError.INSUFFICIENT_COINS,
                            error = result.error.toUIText(),
                        )
                    }
                }
            }
        }
    }

    private fun emit(effect: CoverLetterEffect) {
        viewModelScope.launch { effectChannel.send(effect) }
    }

    private fun attemptKey(id: Long) = "ats_cover_letter_attempted_$id"
}
