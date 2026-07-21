package com.iti.careerpilot.reports.presentation

import com.iti.careerpilot.reports.domain.usecase.GetSessionHistoryUseCase
import com.iti.careerpilot.reports.presentation.screen.history.viewmodel.SessionHistoryViewModel
import com.iti.careerpilot.reports.presentation.screen.history.contract.SessionHistoryAction
import com.iti.careerpilot.reports.presentation.screen.history.contract.SessionHistoryEvent
import com.iti.careerpilot.reports.testutil.FakeNetworkMonitor
import com.iti.careerpilot.reports.testutil.FakeReportsRepository
import com.iti.careerpilot.reports.testutil.MainDispatcherRule
import com.iti.common.error.NetworkError
import com.iti.common.result.CareerPilotResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SessionHistoryViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `load success exposes immutable content and session click event`() =
        runTest(mainDispatcherRule.dispatcher) {
            val repository = FakeReportsRepository()
            val viewModel = SessionHistoryViewModel(
                getSessionHistory = GetSessionHistoryUseCase(repository),
                networkMonitor = FakeNetworkMonitor(),
            )
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.state.collect {}
            }
            advanceUntilIdle()

            assertNotNull(viewModel.state.value.content)
            assertEquals(82, viewModel.state.value.content?.averageScore)

            val event = async { viewModel.events.first() }
            viewModel.onAction(SessionHistoryAction.SessionClicked("session-1"))
            assertEquals(
                SessionHistoryEvent.NavigateToSessionDetails("session-1"),
                event.await(),
            )
        }

    @Test
    fun `retry recovers from error and offline state does not clear content`() =
        runTest(mainDispatcherRule.dispatcher) {
            val repository = FakeReportsRepository().apply {
                historyResult = CareerPilotResult.Error(NetworkError.SERVER)
            }
            val networkMonitor = FakeNetworkMonitor()
            val viewModel = SessionHistoryViewModel(
                getSessionHistory = GetSessionHistoryUseCase(repository),
                networkMonitor = networkMonitor,
            )
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.state.collect {}
            }
            advanceUntilIdle()
            assertNotNull(viewModel.state.value.error)

            repository.historyResult = CareerPilotResult.Success(
                listOf(com.iti.careerpilot.reports.testutil.sampleSession),
            )
            viewModel.onAction(SessionHistoryAction.Retry)
            advanceUntilIdle()
            networkMonitor.setOnline(false)
            advanceUntilIdle()

            assertEquals(2, repository.historyCalls)
            assertNotNull(viewModel.state.value.content)
            assertFalse(viewModel.state.value.isOnline)
        }
}
