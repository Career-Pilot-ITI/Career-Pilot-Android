package com.iti.careerpilot.ats.presentation.jobdetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iti.careerpilot.ats.domain.usecase.GetWorkspaceUseCase
import com.iti.careerpilot.ats.domain.util.JobUrlParser
import com.iti.common.result.CareerPilotResult
import com.iti.common.util.toUIText
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class JobDetailsViewModel @Inject constructor(
    private val getWorkspace: GetWorkspaceUseCase,
) : ViewModel() {
    private var workspaceId: Long? = null
    private val _state = MutableStateFlow(JobDetailsUiState())
    val state = _state.asStateFlow()

    private val effectChannel = Channel<JobDetailsEffect>(Channel.BUFFERED)
    val effects = effectChannel.receiveAsFlow()

    fun onAction(action: JobDetailsAction) {
        when (action) {
            JobDetailsAction.RetryClicked -> workspaceId?.let(::loadWorkspace)
            JobDetailsAction.ScoreClicked -> workspaceId?.let {
                emit(JobDetailsEffect.NavigateToScore(it))
            }
            JobDetailsAction.ExternalLinkClicked -> {
                val job = _state.value.workspace?.job ?: return
                listOfNotNull(job.sourceUrl, job.applicationUrl)
                    .firstOrNull(JobUrlParser::isValidHttpsUrl)
                    ?.let { emit(JobDetailsEffect.OpenExternalUrl(it)) }
            }
        }
    }

    fun loadWorkspace(workspaceId: Long) {
        if (_state.value.isLoading && _state.value.workspace != null) return
        this.workspaceId = workspaceId
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            when (val result = getWorkspace(workspaceId)) {
                is CareerPilotResult.Error -> _state.update {
                    it.copy(isLoading = false, error = result.error.toUIText())
                }
                is CareerPilotResult.Success -> _state.update {
                    it.copy(isLoading = false, workspace = result.data, error = null)
                }
            }
        }
    }

    private fun emit(effect: JobDetailsEffect) {
        viewModelScope.launch { effectChannel.send(effect) }
    }
}
