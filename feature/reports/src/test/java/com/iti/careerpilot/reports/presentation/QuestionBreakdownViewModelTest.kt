package com.iti.careerpilot.reports.presentation

import androidx.lifecycle.SavedStateHandle
import com.iti.careerpilot.reports.domain.usecase.GetQuestionBreakdownUseCase
import com.iti.careerpilot.reports.presentation.screen.breakdown.viewmodel.QuestionBreakdownViewModel
import com.iti.careerpilot.reports.presentation.screen.breakdown.contract.QuestionBreakdownAction
import com.iti.careerpilot.reports.testutil.FakeNetworkMonitor
import com.iti.careerpilot.reports.testutil.FakeReportsRepository
import com.iti.careerpilot.reports.testutil.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class QuestionBreakdownViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `question selection is validated and restored`() = runTest(mainDispatcherRule.dispatcher) {
        val savedStateHandle = SavedStateHandle()
        val repository = FakeReportsRepository()
        val firstViewModel = QuestionBreakdownViewModel(
            getQuestionBreakdown = GetQuestionBreakdownUseCase(repository),
            savedStateHandle = savedStateHandle,
            networkMonitor = FakeNetworkMonitor(),
        )
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            firstViewModel.state.collect {}
        }
        firstViewModel.onAction(QuestionBreakdownAction.Load("session-1"))
        advanceUntilIdle()
        firstViewModel.onAction(QuestionBreakdownAction.QuestionSelected("question-2"))
        firstViewModel.onAction(QuestionBreakdownAction.QuestionSelected("missing"))
        advanceUntilIdle()

        assertEquals("question-2", firstViewModel.state.value.selectedQuestionId)

        val restoredViewModel = QuestionBreakdownViewModel(
            getQuestionBreakdown = GetQuestionBreakdownUseCase(repository),
            savedStateHandle = savedStateHandle,
            networkMonitor = FakeNetworkMonitor(),
        )
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            restoredViewModel.state.collect {}
        }
        restoredViewModel.onAction(QuestionBreakdownAction.Load("session-1"))
        advanceUntilIdle()

        assertEquals("question-2", restoredViewModel.state.value.selectedQuestionId)
        assertEquals("question-2", restoredViewModel.state.value.selectedQuestion?.id)
    }
}
