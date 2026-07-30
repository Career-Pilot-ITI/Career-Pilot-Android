package com.iti.careerpilot.reports.presentation.screen.history.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import androidx.paging.map
import com.iti.careerpilot.reports.domain.usecase.GetSessionHistoryUseCase
import com.iti.careerpilot.reports.presentation.screen.history.contract.SessionHistoryAction
import com.iti.careerpilot.reports.presentation.screen.history.contract.SessionHistoryEvent
import com.iti.careerpilot.reports.presentation.screen.history.contract.SessionHistoryState
import com.iti.careerpilot.reports.presentation.screen.history.paging.SessionHistoryPagingSource
import com.iti.careerpilot.reports.presentation.screen.history.uimodels.toUiModel
import com.iti.common.network.NetworkMonitor
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class SessionHistoryViewModel @Inject constructor(
    getSessionHistory: GetSessionHistoryUseCase,
    networkMonitor: NetworkMonitor,
) : ViewModel() {
    val sessions = Pager(
        config = PagingConfig(
            pageSize = PAGE_SIZE,
            initialLoadSize = PAGE_SIZE,
            prefetchDistance = PREFETCH_DISTANCE,
            enablePlaceholders = false,
        ),
        pagingSourceFactory = {
            SessionHistoryPagingSource(getSessionHistory)
        },
    ).flow
        .map { pagingData ->
            pagingData.map { session -> session.toUiModel() }
        }
        .cachedIn(viewModelScope)

    val state = networkMonitor.isOnline
        .map { isOnline -> SessionHistoryState(isOnline = isOnline) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Companion.WhileSubscribed(5_000L),
            initialValue = SessionHistoryState(isOnline = networkMonitor.isOnline.value),
        )

    private val eventChannel = Channel<SessionHistoryEvent>(Channel.Factory.BUFFERED)
    val events = eventChannel.receiveAsFlow()

    fun onAction(action: SessionHistoryAction) {
        when (action) {
            is SessionHistoryAction.SessionClicked -> {
                if (action.sessionId > 0L) {
                    eventChannel.trySend(SessionHistoryEvent.NavigateToSessionDetails(action.sessionId))
                }
            }
        }
    }

    private companion object {
        const val PAGE_SIZE = 10
        const val PREFETCH_DISTANCE = 3
    }
}
