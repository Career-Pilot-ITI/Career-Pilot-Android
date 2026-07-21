package com.iti.careerpilot.reports.presentation

import com.iti.careerpilot.reports.domain.usecase.GetReportDetailsUseCase
import com.iti.careerpilot.reports.presentation.screen.details.viewmodel.ReportDetailsViewModel
import com.iti.careerpilot.reports.presentation.screen.details.contract.ReportDetailsAction
import com.iti.careerpilot.reports.presentation.screen.details.contract.ReportDetailsEvent
import com.iti.careerpilot.reports.testutil.FakeNetworkMonitor
import com.iti.careerpilot.reports.testutil.FakeReportsRepository
import com.iti.careerpilot.reports.testutil.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ReportDetailsViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `valid report loads and emits question breakdown navigation`() =
        runTest(mainDispatcherRule.dispatcher) {
            val viewModel = ReportDetailsViewModel(
                getReportDetails = GetReportDetailsUseCase(FakeReportsRepository()),
                networkMonitor = FakeNetworkMonitor(),
            )
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.state.collect {}
            }
            viewModel.onAction(ReportDetailsAction.Load("session-1"))
            advanceUntilIdle()
            assertNotNull(viewModel.state.value.content)

            val event = async { viewModel.events.first() }
            viewModel.onAction(ReportDetailsAction.QuestionBreakdownClicked)
            assertEquals(
                ReportDetailsEvent.NavigateToQuestionBreakdown("session-1"),
                event.await(),
            )
        }
}
