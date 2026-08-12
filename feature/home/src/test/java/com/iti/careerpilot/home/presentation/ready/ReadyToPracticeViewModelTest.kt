package com.iti.careerpilot.home.presentation.ready

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.resetMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ReadyToPracticeViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `begin interview forwards optional workspace`() = runTest(dispatcher) {
        val viewModel = ReadyToPracticeViewModel(SavedStateHandle())
        viewModel.onAction(ReadyToPracticeAction.Initial(5L, "Android", 42L))
        viewModel.onAction(ReadyToPracticeAction.MicrophonePermissionChanged(true))
        val event = async { viewModel.events.first() }

        viewModel.onAction(ReadyToPracticeAction.BeginInterviewClicked)
        runCurrent()

        assertEquals(
            ReadyToPracticeEvent.NavigateToPractice(trackId = 5L, workspaceId = 42L),
            event.await(),
        )
    }
}
