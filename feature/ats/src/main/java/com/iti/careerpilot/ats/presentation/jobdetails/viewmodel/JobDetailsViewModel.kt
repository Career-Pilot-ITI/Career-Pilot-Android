package com.iti.careerpilot.ats.presentation.jobdetails.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iti.careerpilot.ats.domain.usecase.GetWorkspaceUseCase
import com.iti.careerpilot.ats.presentation.jobdetails.state.JobDetailsAction
import com.iti.careerpilot.ats.presentation.jobdetails.state.JobDetailsEffect
import com.iti.careerpilot.ats.presentation.jobdetails.state.JobDetailsUiState
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
    private val _state = MutableStateFlow(JobDetailsUiState())
    val state = _state.asStateFlow()

    private val effectChannel = Channel<JobDetailsEffect>(Channel.BUFFERED)
    val effects = effectChannel.receiveAsFlow()

    private var workspaceId: Long? = null

    fun onAction(action: JobDetailsAction) {
        when (action) {
            is JobDetailsAction.Initial -> loadWorkspace(action.workspaceId)
            JobDetailsAction.Retry -> workspaceId?.let(::loadWorkspace)
            JobDetailsAction.StartScoring -> workspaceId?.let { id ->
                viewModelScope.launch {
                    effectChannel.send(JobDetailsEffect.OpenScore(id))
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
                    it.copy(
                        workspace = result.data,
                        isLoading = false,
                        error = null,
                    )
                }
            }
        }
    }
}
