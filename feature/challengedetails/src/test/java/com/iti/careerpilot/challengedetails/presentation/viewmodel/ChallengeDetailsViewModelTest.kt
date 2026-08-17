package com.iti.careerpilot.challengedetails.presentation.viewmodel

import com.iti.careerpilot.challengedetails.domain.repository.ChallengeDetailsRepository
import com.iti.careerpilot.challengedetails.presentation.action.ChallengeDetailsAction
import com.iti.careerpilot.challengedetails.presentation.event.ChallengeDetailsEvent
import com.iti.careerpilot.challengefirestore.Challenge
import com.iti.careerpilot.challengefirestore.ChallengeType
import com.iti.careerpilot.challengefirestore.ChallengeVisibility
import com.iti.careerpilot.challengefirestore.SeniorityLevel
import com.iti.common.error.FirebaseError
import com.iti.common.result.CareerPilotResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ChallengeDetailsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val dummyAudioChallenge = Challenge(
        id = "audio_1",
        creatorId = 1L,
        creatorUsername = "tester",
        creatorName = "Tester User",
        trackId = 10L,
        trackName = "Android",
        visibility = ChallengeVisibility.PUBLIC,
        seniorityLevel = SeniorityLevel.MID_LEVEL,
        creationDate = 1700000000L,
        invitationCode = "INVITE123",
        questions = emptyList(),
        type = ChallengeType.AUDIO_ONLY,
    )

    private val dummyVideoChallenge = Challenge(
        id = "video_1",
        creatorId = 1L,
        creatorUsername = "tester",
        creatorName = "Tester User",
        trackId = 10L,
        trackName = "Android",
        visibility = ChallengeVisibility.PUBLIC,
        seniorityLevel = SeniorityLevel.MID_LEVEL,
        creationDate = 1700000000L,
        invitationCode = "INVITE123",
        questions = emptyList(),
        type = ChallengeType.VIDEO_AND_AUDIO,
    )

    private class FakeChallengeDetailsRepository : ChallengeDetailsRepository {
        var getChallengeResult: CareerPilotResult<Challenge, FirebaseError> =
            CareerPilotResult.Success(
                Challenge(
                    id = "audio_1",
                    creatorId = 1L,
                    creatorUsername = "tester",
                    creatorName = "Tester User",
                    trackId = 10L,
                    trackName = "Android",
                    visibility = ChallengeVisibility.PUBLIC,
                    seniorityLevel = SeniorityLevel.MID_LEVEL,
                    creationDate = 1700000000L,
                    invitationCode = "INVITE123",
                    questions = emptyList(),
                    type = ChallengeType.AUDIO_ONLY,
                )
            )
        var getChallengeCallCount = 0
        var lastChallengeId: String? = null

        override suspend fun getChallenge(challengeId: String): CareerPilotResult<Challenge, FirebaseError> {
            getChallengeCallCount++
            lastChallengeId = challengeId
            return getChallengeResult
        }
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(
        repository: FakeChallengeDetailsRepository = FakeChallengeDetailsRepository()
    ): Pair<ChallengeDetailsViewModel, FakeChallengeDetailsRepository> {
        return Pair(ChallengeDetailsViewModel(repository = repository), repository)
    }

    @Test
    fun `ShareClicked action sets isShareDialogVisible to true`() = runTest {
        val (viewModel, _) = createViewModel()

        assertFalse(viewModel.state.value.isShareDialogVisible)

        viewModel.onAction(ChallengeDetailsAction.ShareClicked)

        assertTrue(viewModel.state.value.isShareDialogVisible)
    }

    @Test
    fun `DismissShareDialog action sets isShareDialogVisible to false`() = runTest {
        val (viewModel, _) = createViewModel()

        viewModel.onAction(ChallengeDetailsAction.ShareClicked)
        assertTrue(viewModel.state.value.isShareDialogVisible)

        viewModel.onAction(ChallengeDetailsAction.DismissShareDialog)
        assertFalse(viewModel.state.value.isShareDialogVisible)
    }

    @Test
    fun `Initial action fetches challenge successfully and updates state`() = runTest {
        val repo = FakeChallengeDetailsRepository().apply {
            getChallengeResult = CareerPilotResult.Success(dummyAudioChallenge)
        }
        val (viewModel, _) = createViewModel(repository = repo)

        viewModel.onAction(ChallengeDetailsAction.Initial("audio_1"))
        testScheduler.advanceUntilIdle()

        assertEquals(1, repo.getChallengeCallCount)
        assertEquals("audio_1", repo.lastChallengeId)
        assertEquals(dummyAudioChallenge, viewModel.state.value.challenge)
        assertFalse(viewModel.state.value.isLoading)
        assertNull(viewModel.state.value.error)
    }

    @Test
    fun `Initial action on failure updates error state`() = runTest {
        val repo = FakeChallengeDetailsRepository().apply {
            getChallengeResult = CareerPilotResult.Error(FirebaseError.NOT_FOUND)
        }
        val (viewModel, _) = createViewModel(repository = repo)

        viewModel.onAction(ChallengeDetailsAction.Initial("missing_id"))
        testScheduler.advanceUntilIdle()

        assertEquals(1, repo.getChallengeCallCount)
        assertNull(viewModel.state.value.challenge)
        assertFalse(viewModel.state.value.isLoading)
        assertEquals(FirebaseError.NOT_FOUND.toString(), viewModel.state.value.error)
    }

    @Test
    fun `MicrophonePermissionChanged updates isMicrophoneGranted`() = runTest {
        val (viewModel, _) = createViewModel()

        viewModel.onAction(ChallengeDetailsAction.MicrophonePermissionChanged(isGranted = true))
        assertTrue(viewModel.state.value.isMicrophoneGranted)

        viewModel.onAction(ChallengeDetailsAction.MicrophonePermissionChanged(isGranted = false))
        assertFalse(viewModel.state.value.isMicrophoneGranted)
    }

    @Test
    fun `CameraPermissionChanged updates isCameraGranted`() = runTest {
        val (viewModel, _) = createViewModel()

        viewModel.onAction(ChallengeDetailsAction.CameraPermissionChanged(isGranted = true))
        assertTrue(viewModel.state.value.isCameraGranted)

        viewModel.onAction(ChallengeDetailsAction.CameraPermissionChanged(isGranted = false))
        assertFalse(viewModel.state.value.isCameraGranted)
    }

    @Test
    fun `PermissionDialogDismissed resets showMicPermissionDialog`() = runTest {
        val (viewModel, _) = createViewModel()

        viewModel.onAction(ChallengeDetailsAction.MicrophoneRowClicked)
        assertTrue(viewModel.state.value.showMicPermissionDialog)

        viewModel.onAction(ChallengeDetailsAction.PermissionDialogDismissed)
        assertFalse(viewModel.state.value.showMicPermissionDialog)
    }

    @Test
    fun `CameraPermissionDialogDismissed resets showCameraPermissionDialog`() = runTest {
        val (viewModel, _) = createViewModel()

        viewModel.onAction(ChallengeDetailsAction.CameraRowClicked)
        assertTrue(viewModel.state.value.showCameraPermissionDialog)

        viewModel.onAction(ChallengeDetailsAction.CameraPermissionDialogDismissed)
        assertFalse(viewModel.state.value.showCameraPermissionDialog)
    }

    @Test
    fun `MicrophoneRowClicked shows mic permission dialog and hides camera dialog`() = runTest {
        val (viewModel, _) = createViewModel()

        viewModel.onAction(ChallengeDetailsAction.MicrophoneRowClicked)
        assertTrue(viewModel.state.value.showMicPermissionDialog)
        assertFalse(viewModel.state.value.showCameraPermissionDialog)
    }

    @Test
    fun `CameraRowClicked shows both mic and camera permission dialog flags`() = runTest {
        val (viewModel, _) = createViewModel()

        viewModel.onAction(ChallengeDetailsAction.CameraRowClicked)
        assertTrue(viewModel.state.value.showMicPermissionDialog)
        assertTrue(viewModel.state.value.showCameraPermissionDialog)
    }

    @Test
    fun `OnBackClicked emits NavigateBack event`() = runTest {
        val (viewModel, _) = createViewModel()
        val events = mutableListOf<ChallengeDetailsEvent>()
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.events.toList(events)
        }

        viewModel.onAction(ChallengeDetailsAction.OnBackClicked)
        testScheduler.advanceUntilIdle()

        assertEquals(1, events.size)
        assertTrue(events.first() is ChallengeDetailsEvent.NavigateBack)
        job.cancel()
    }

    @Test
    fun `BeginChallengeClicked when audio only and mic granted emits NavigateToPractice`() = runTest {
        val repo = FakeChallengeDetailsRepository().apply {
            getChallengeResult = CareerPilotResult.Success(dummyAudioChallenge)
        }
        val (viewModel, _) = createViewModel(repository = repo)
        val events = mutableListOf<ChallengeDetailsEvent>()
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.events.toList(events)
        }

        viewModel.onAction(ChallengeDetailsAction.Initial("audio_1"))
        testScheduler.advanceUntilIdle()

        viewModel.onAction(ChallengeDetailsAction.MicrophonePermissionChanged(isGranted = true))
        viewModel.onAction(ChallengeDetailsAction.BeginChallengeClicked)
        testScheduler.advanceUntilIdle()

        assertEquals(1, events.size)
        val event = events.first() as? ChallengeDetailsEvent.NavigateToPractice
        assertEquals(dummyAudioChallenge, event?.challenge)
        job.cancel()
    }

    @Test
    fun `BeginChallengeClicked when mic not granted shows mic permission dialog and does not emit event`() = runTest {
        val repo = FakeChallengeDetailsRepository().apply {
            getChallengeResult = CareerPilotResult.Success(dummyAudioChallenge)
        }
        val (viewModel, _) = createViewModel(repository = repo)
        val events = mutableListOf<ChallengeDetailsEvent>()
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.events.toList(events)
        }

        viewModel.onAction(ChallengeDetailsAction.Initial("audio_1"))
        testScheduler.advanceUntilIdle()

        viewModel.onAction(ChallengeDetailsAction.MicrophonePermissionChanged(isGranted = false))
        viewModel.onAction(ChallengeDetailsAction.BeginChallengeClicked)
        testScheduler.advanceUntilIdle()

        assertTrue(viewModel.state.value.showMicPermissionDialog)
        assertTrue(events.isEmpty())
        job.cancel()
    }

    @Test
    fun `BeginChallengeClicked for video challenge when camera not granted shows camera dialog and does not emit event`() = runTest {
        val repo = FakeChallengeDetailsRepository().apply {
            getChallengeResult = CareerPilotResult.Success(dummyVideoChallenge)
        }
        val (viewModel, _) = createViewModel(repository = repo)
        val events = mutableListOf<ChallengeDetailsEvent>()
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.events.toList(events)
        }

        viewModel.onAction(ChallengeDetailsAction.Initial("video_1"))
        testScheduler.advanceUntilIdle()

        viewModel.onAction(ChallengeDetailsAction.MicrophonePermissionChanged(isGranted = true))
        viewModel.onAction(ChallengeDetailsAction.CameraPermissionChanged(isGranted = false))
        viewModel.onAction(ChallengeDetailsAction.BeginChallengeClicked)
        testScheduler.advanceUntilIdle()

        assertTrue(viewModel.state.value.showCameraPermissionDialog)
        assertTrue(events.isEmpty())
        job.cancel()
    }
}
