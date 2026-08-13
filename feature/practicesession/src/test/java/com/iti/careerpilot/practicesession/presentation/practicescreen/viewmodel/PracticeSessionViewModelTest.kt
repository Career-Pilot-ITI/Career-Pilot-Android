package com.iti.careerpilot.practicesession.presentation.practicescreen.viewmodel

import android.content.Context
import androidx.camera.core.ImageProxy
import androidx.lifecycle.SavedStateHandle
import com.iti.careerpilot.bodylanguage.BodyLanguageAnalyzer
import com.iti.careerpilot.practicesession.data.audio.AmplitudeNormalizer
import com.iti.careerpilot.practicesession.data.tts.TextToSpeechManager
import com.iti.careerpilot.practicesession.domain.audio.AudioPlayer
import com.iti.careerpilot.practicesession.domain.audio.models.AudioTrack
import com.iti.careerpilot.practicesession.domain.models.AnswerRequest
import com.iti.careerpilot.practicesession.domain.models.AnswerResponse
import com.iti.careerpilot.practicesession.domain.models.AudioAttachment
import com.iti.careerpilot.practicesession.domain.models.CreateSessionRequest
import com.iti.careerpilot.practicesession.domain.models.CurrentQuestion
import com.iti.careerpilot.practicesession.domain.models.Score
import com.iti.careerpilot.practicesession.domain.models.Session
import com.iti.careerpilot.practicesession.domain.models.SessionResult
import com.iti.careerpilot.practicesession.domain.recording.VoiceRecorder
import com.iti.careerpilot.practicesession.domain.recording.models.RecordingDetails
import com.iti.careerpilot.practicesession.domain.repo.SessionRepo
import com.iti.careerpilot.practicesession.presentation.practicescreen.action.PracticeSessionAction
import com.iti.careerpilot.practicesession.presentation.practicescreen.event.PracticeSessionEvent
import com.iti.careerpilot.whisper.domain.WhisperEngine
import com.iti.common.error.NetworkError
import com.iti.common.result.CareerPilotResult
import com.iti.core.datastore.models.AccountInfo
import com.iti.core.datastore.models.UserProfile
import com.iti.core.datastore.repo.UserProfileRepo
import com.iti.core.model.bodylanguage.BodyLanguageMetrics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File
import java.lang.reflect.Proxy

@OptIn(ExperimentalCoroutinesApi::class)
class PracticeSessionViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val dummyQuestion = CurrentQuestion(
        id = 1L,
        sessionId = 100L,
        questionText = "Tell me about yourself",
        questionOrder = 1,
        createdAt = "2026-08-11T00:00:00Z"
    )

    private val dummySession = Session(
        sessionId = 100L,
        status = "IN_PROGRESS",
        trackName = "Android Engineer",
        targetDurationMinutes = 20,
        maxQuestions = 10,
        answeredCount = 0,
        startedAt = "2026-08-11T00:00:00Z",
        updatedAt = "2026-08-11T00:00:00Z",
        currentQuestion = dummyQuestion,
        answeredQuestions = emptyList()
    )

    private class FakeSessionRepo(
        private val session: Session,
        var submitStatus: String = "IN_PROGRESS"
    ) : SessionRepo {
        var lastAnswerRequest: AnswerRequest? = null
        var lastUploadedFile: File? = null

        override suspend fun createNewSession(request: CreateSessionRequest): CareerPilotResult<Session, NetworkError> {
            return CareerPilotResult.Success(session)
        }

        override suspend fun uploadAudio(file: File, onProgress: (Int) -> Unit): CareerPilotResult<AudioAttachment, NetworkError> {
            lastUploadedFile = file
            onProgress(100)
            return CareerPilotResult.Success(
                AudioAttachment(
                    id = 1L,
                    type = "audio/wav",
                    originalName = "audio.wav",
                    url = "https://storage.careerpilot.com/audio.wav",
                    sizeBytes = 1024L,
                    createdAt = "2026-08-11T00:00:00Z"
                )
            )
        }

        override suspend fun submitAnswer(sessionId: Long, request: AnswerRequest): CareerPilotResult<AnswerResponse, NetworkError> {
            lastAnswerRequest = request
            return CareerPilotResult.Success(
                AnswerResponse(
                    sessionStatus = submitStatus,
                    score = Score(
                        id = 1L,
                        sessionQuestionId = 1L,
                        contentRelevance = 90,
                        clarity = 85,
                        confidence = 88,
                        pacing = 80,
                        fillerWords = 2,
                        overallScore = 86,
                        coachingTip = "Great job!",
                        createdAt = "2026-08-11T00:00:00Z"
                    ),
                    nextQuestion = if (submitStatus == "IN_PROGRESS") session.currentQuestion else null
                )
            )
        }

        override suspend fun getSessionFeedback(sessionId: Long): CareerPilotResult<SessionResult, NetworkError> {
            return CareerPilotResult.Error(NetworkError.SERVER)
        }

        override suspend fun restartOldSession(sessionId: Long): CareerPilotResult<Session, NetworkError> {
            return CareerPilotResult.Success(session.copy(sessionId = sessionId))
        }
    }

    private class FakeVoiceRecorder : VoiceRecorder {
        private val _recordingDetails = MutableStateFlow(RecordingDetails())
        override val recordingDetails = _recordingDetails.asStateFlow()

        var started = false
        var stopped = false
        var cancelled = false

        fun emitDetails(details: RecordingDetails) {
            _recordingDetails.value = details
        }

        override fun start() { started = true }
        override fun pause() {}
        override fun stop() { stopped = true }
        override fun resume() {}
        override fun cancel() { cancelled = true }
    }

    private class FakeAudioPlayer : AudioPlayer {
        private val _activeTrack = MutableStateFlow(AudioTrack())
        override val activeTrack = _activeTrack.asStateFlow()

        var stopped = false

        override fun play(filePath: String, onComplete: () -> Unit) {}
        override fun prepare(filePath: String) {}
        override fun pause() {}
        override fun resume() {}
        override fun stop() { stopped = true }
        override fun seekTo(positionMs: Long) {}
    }

    private class FakeWhisperEngine(val transcription: String = "This is my transcribed response.") : WhisperEngine {
        var transcribeCalled = false

        override suspend fun transcribe(filePath: String): Result<String> {
            transcribeCalled = true
            return Result.success(transcription)
        }
    }

    private class FakeBodyLanguageAnalyzer : BodyLanguageAnalyzer {
        private var _isRunning = false
        override fun enablePostureTracking(enabled: Boolean) {}

        override fun enableHandTracking(enabled: Boolean) {}

        override val isRunning: Boolean
            get() = _isRunning

        private var _isRecordingActive = true
        override val isRecordingActive: Boolean get() = _isRecordingActive

        var startCalled = false
        var stopCalled = false
        var processImageCalled = false
        var finalizeSessionCalled = false

        override fun start() {
            _isRunning = true
            startCalled = true
        }

        override fun processImage(imageProxy: ImageProxy) {
            processImageCalled = true
            imageProxy.close()
        }

        override fun setRecordingActive(active: Boolean) {
            _isRecordingActive = active
        }

        override fun pauseRecording(timestampMs: Long) {
            _isRecordingActive = false
        }

        override fun resumeRecording(timestampMs: Long) {
            _isRecordingActive = true
        }

        override fun stop() {
            _isRunning = false
            stopCalled = true
        }

        override suspend fun finalizeSession(): BodyLanguageMetrics {
            finalizeSessionCalled = true
            return BodyLanguageMetrics(
                sessionDurationMs = 12000L,
                averageSmile = 0.6f,
                maxSmile = 0.9f,
                eyeContactPercentage = 80f,
                timeLookingAwayMs = 1000L,
                faceLostCount = 0,
                averageTorsoLeanDeg = 2f,
                averageShoulderTiltDeg = 1f,
                slouchPercentage = 5f,
                postureChanges = 1,
                handsVisiblePercentage = 70f,
                handToFaceTouchCount = 1,
                fidgetScore = 0.1f,
                keyMoments = emptyList()
            )
        }
    }

    private class FakeUserProfileRepo(initialProfile: UserProfile = UserProfile()) : UserProfileRepo {
        private val _userProfile = MutableStateFlow(initialProfile)
        override val userProfile = _userProfile.asStateFlow()

        override suspend fun updateUserProfile(updateBlock: (UserProfile) -> UserProfile) {
            _userProfile.value = updateBlock(_userProfile.value)
        }

        override suspend fun readUserProfile(): UserProfile = _userProfile.value

        override suspend fun clearUserProfile() {
            _userProfile.value = UserProfile()
        }

        override suspend fun setBodyLanguageConsent(given: Boolean) {
            updateUserProfile { profile ->
                profile.copy(
                    account = profile.account.copy(bodyLanguageConsentGiven = given)
                )
            }
        }
    }

    private fun createDummyContext(): Context {
        return object : android.content.ContextWrapper(null) {
            override fun getApplicationContext(): Context = this
            override fun getPackageName(): String = "com.iti.careerpilot"
        }
    }

    private val createdViewModels = mutableListOf<PracticeSessionViewModel>()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        createdViewModels.forEach { it.onCleared() }
        createdViewModels.clear()
        Dispatchers.resetMain()
    }

    private fun runSessionTest(
        block: suspend TestScope.() -> Unit
    ) = runTest {
        try {
            block()
        } finally {
            createdViewModels.forEach { it.onCleared() }
            createdViewModels.clear()
        }
    }

    private fun TestScope.createViewModel(
        savedStateHandle: SavedStateHandle = SavedStateHandle(),
        sessionRepo: SessionRepo = FakeSessionRepo(dummySession),
        voiceRecorder: VoiceRecorder = FakeVoiceRecorder(),
        audioPlayer: AudioPlayer = FakeAudioPlayer(),
        whisperEngine: WhisperEngine = FakeWhisperEngine(),
        bodyLanguageAnalyzer: BodyLanguageAnalyzer = FakeBodyLanguageAnalyzer(),
        userProfileRepo: UserProfileRepo = FakeUserProfileRepo(),
        sessionCache: com.iti.careerpilot.ai.cache.InMemorySessionCache = com.iti.careerpilot.ai.cache.InMemorySessionCache(),
    ): PracticeSessionViewModel {
        val ttsManager = TextToSpeechManager(createDummyContext())
        val amplitudeNormalizer = AmplitudeNormalizer()
        val vm = PracticeSessionViewModel(
            savedStateHandle = savedStateHandle,
            sessionRepo = sessionRepo,
            voiceRecorder = voiceRecorder,
            audioPlayer = audioPlayer,
            whisperEngine = whisperEngine,
            textToSpeechManager = ttsManager,
            amplitudeNormalizer = amplitudeNormalizer,
            bodyLanguageAnalyzer = bodyLanguageAnalyzer,
            userProfileRepo = userProfileRepo,
            sessionCache = sessionCache,
            defaultDispatcher = testDispatcher
        ).also { createdViewModels.add(it) }
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            vm.state.collect {}
        }
        return vm
    }

    @Test
    fun `CreateNewPracticeSession loads session and updates state`() = runSessionTest {
        val viewModel = createViewModel()
        viewModel.onAction(PracticeSessionAction.CreateNewPracticeSession(trackId = 1L, isVideoSession = false))
        testScheduler.runCurrent()

        assertEquals(dummySession.sessionId, viewModel.state.value.sessionId)
        assertEquals(dummyQuestion.questionText, viewModel.state.value.currentSession?.currentQuestion?.questionText)
        assertFalse(viewModel.state.value.isLoadingSession)
    }

    @Test
    fun `RestartPracticeSession restarts old session and updates state`() = runSessionTest {
        val viewModel = createViewModel()
        viewModel.onAction(PracticeSessionAction.RestartPracticeSession(sessionId = 200L, isVideoSession = false))
        testScheduler.runCurrent()

        assertEquals(200L, viewModel.state.value.sessionId)
    }

    @Test
    fun `Paid user in video session enables body language`() = runSessionTest {
        val profile = UserProfile(
            account = AccountInfo(
                subscriptionTier = "PLUS",
                bodyLanguageConsentGiven = true
            )
        )
        val userProfileRepo = FakeUserProfileRepo(profile)
        val viewModel = createViewModel(userProfileRepo = userProfileRepo)

        viewModel.onAction(PracticeSessionAction.CreateNewPracticeSession(trackId = 1L, isVideoSession = true))
        testScheduler.runCurrent()

        assertTrue(viewModel.state.value.bodyLanguageEnabled)
        assertTrue(viewModel.state.value.bodyLanguageConsentGiven)
    }

    @Test
    fun `Free user in video session disables body language`() = runSessionTest {
        val profile = UserProfile(
            account = AccountInfo(
                subscriptionTier = "FREE",
                bodyLanguageConsentGiven = true
            )
        )
        val userProfileRepo = FakeUserProfileRepo(profile)
        val viewModel = createViewModel(userProfileRepo = userProfileRepo)

        viewModel.onAction(PracticeSessionAction.CreateNewPracticeSession(trackId = 1L, isVideoSession = true))
        testScheduler.runCurrent()

        assertFalse(viewModel.state.value.bodyLanguageEnabled)
    }

    @Test
    fun `OnFrame action delivers imageProxy to bodyLanguageAnalyzer`() = runSessionTest {
        val fakeAnalyzer = FakeBodyLanguageAnalyzer()
        val viewModel = createViewModel(bodyLanguageAnalyzer = fakeAnalyzer)

        var imageProxyClosed = false
        val dummyImageProxy = Proxy.newProxyInstance(
            ImageProxy::class.java.classLoader,
            arrayOf(ImageProxy::class.java)
        ) { _, method, _ ->
            if (method.name == "close") {
                imageProxyClosed = true
            }
            null
        } as ImageProxy

        viewModel.onAction(PracticeSessionAction.OnFrame(dummyImageProxy))
        testScheduler.runCurrent()

        assertTrue(fakeAnalyzer.processImageCalled)
        assertTrue(imageProxyClosed)
    }

    @Test
    fun `ToggleCameraPreview updates isCameraPreviewVisible in state`() = runSessionTest {
        val viewModel = createViewModel()

        viewModel.onAction(PracticeSessionAction.ToggleCameraPreview(visible = true))
        testScheduler.runCurrent()
        assertTrue(viewModel.state.value.isCameraPreviewVisible)

        viewModel.onAction(PracticeSessionAction.ToggleCameraPreview(visible = false))
        testScheduler.runCurrent()
        assertFalse(viewModel.state.value.isCameraPreviewVisible)
    }

    @Test
    fun `DiscardCurrentAnswer cancels voiceRecorder and resets recorded audio state`() = runSessionTest {
        val fakeRecorder = FakeVoiceRecorder()
        val viewModel = createViewModel(voiceRecorder = fakeRecorder)

        viewModel.onAction(PracticeSessionAction.DiscardCurrentAnswer)
        testScheduler.runCurrent()

        assertTrue(fakeRecorder.cancelled)
        assertEquals(null, viewModel.state.value.recordedAudioPath)
        assertEquals(null, viewModel.state.value.transcription)
        assertFalse(viewModel.state.value.showDiscardConfirm)
    }

    @Test
    fun `SubmitAnswer with complete status stops body language analyzer and emits NavigateToResult`() = runSessionTest {
        val fakeAnalyzer = FakeBodyLanguageAnalyzer()
        fakeAnalyzer.start()
        val fakeRepo = FakeSessionRepo(dummySession, submitStatus = "READY_TO_COMPLETE")
        val fakeRecorder = FakeVoiceRecorder()
        val fakeWhisper = FakeWhisperEngine("My complete answer")

        val tempAudioFile = File.createTempFile("test_answer", ".wav")
        tempAudioFile.deleteOnExit()

        val viewModel = createViewModel(
            sessionRepo = fakeRepo,
            voiceRecorder = fakeRecorder,
            whisperEngine = fakeWhisper,
            bodyLanguageAnalyzer = fakeAnalyzer
        )

        val events = mutableListOf<PracticeSessionEvent>()
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.event.toList(events)
        }

        // Initialize session first
        viewModel.onAction(PracticeSessionAction.CreateNewPracticeSession(trackId = 1L))
        testScheduler.runCurrent()

        // Simulate recording details
        fakeRecorder.emitDetails(
            RecordingDetails(
                filePath = tempAudioFile.absolutePath,
                isRecording = false,
                duration = kotlin.time.Duration.parse("10s"),
                amplitudes = listOf(0.5f, 0.7f, 0.8f)
            )
        )
        testScheduler.runCurrent()

        viewModel.onAction(PracticeSessionAction.SubmitAnswerToCurrentQuestion)
        testScheduler.runCurrent()

        assertTrue(fakeWhisper.transcribeCalled)
        assertNotNull(fakeRepo.lastAnswerRequest)
        assertEquals("My complete answer", fakeRepo.lastAnswerRequest?.transcript)
        assertTrue(fakeAnalyzer.stopCalled)
        assertTrue(fakeAnalyzer.finalizeSessionCalled)

        val navigateEvent = events.filterIsInstance<PracticeSessionEvent.NavigateToResult>().firstOrNull()
        assertNotNull("Expected NavigateToResult event", navigateEvent)
        assertEquals(dummySession.sessionId, navigateEvent!!.sessionId)

        job.cancel()
    }
}
