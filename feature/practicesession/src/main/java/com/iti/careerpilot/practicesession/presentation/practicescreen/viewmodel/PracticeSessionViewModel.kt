package com.iti.careerpilot.practicesession.presentation.practicescreen.viewmodel

import android.speech.tts.UtteranceProgressListener
import android.util.Log
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iti.careerpilot.bodylanguage.BodyLanguageAnalyzer
import com.iti.careerpilot.bodylanguage.model.BodyLanguageMetrics
import com.iti.careerpilot.practicesession.data.audio.AmplitudeNormalizer
import com.iti.careerpilot.practicesession.data.tts.TextToSpeechManager
import com.iti.careerpilot.practicesession.domain.audio.AudioPlayer
import com.iti.careerpilot.practicesession.domain.audio.models.AudioPlaybackState
import com.iti.careerpilot.practicesession.domain.models.AnswerRequest
import com.iti.careerpilot.practicesession.domain.models.AnswerResponse
import com.iti.careerpilot.practicesession.domain.models.AudioAttachment
import com.iti.careerpilot.practicesession.domain.models.CreateSessionRequest
import com.iti.careerpilot.practicesession.domain.models.Session
import com.iti.careerpilot.practicesession.domain.recording.VoiceRecorder
import com.iti.careerpilot.practicesession.domain.repo.SessionRepo
import com.iti.careerpilot.practicesession.presentation.practicescreen.action.PracticeSessionAction
import com.iti.careerpilot.practicesession.presentation.practicescreen.action.PracticeSessionAction.*
import com.iti.careerpilot.practicesession.presentation.practicescreen.event.PracticeSessionEvent
import com.iti.careerpilot.practicesession.presentation.practicescreen.state.PracticeSessionState
import com.iti.careerpilot.practicesession.presentation.practicescreen.state.VolumeBar
import com.iti.careerpilot.whisper.domain.WhisperEngine
import com.iti.common.error.NetworkError
import com.iti.common.error.TranscriptionError
import com.iti.common.result.CareerPilotResult
import com.iti.common.result.onError
import com.iti.common.result.onSuccess
import com.iti.common.util.toUIText
import com.iti.core.datastore.repo.UserProfileRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.sin
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

const val QUESTION_COUNT = 10
const val SESSION_DURATION = 20
const val WAVE_BAR_COUNT = 32

@HiltViewModel
class PracticeSessionViewModel @Inject constructor(
    private val sessionRepo: SessionRepo,
    private val voiceRecorder: VoiceRecorder,
    private val audioPlayer: AudioPlayer,
    private val whisperEngine: WhisperEngine,
    private val textToSpeechManager: TextToSpeechManager,
    private val amplitudeNormalizer: AmplitudeNormalizer,
    private val bodyLanguageAnalyzer: BodyLanguageAnalyzer,
    private val userProfileRepo: UserProfileRepo,
) : ViewModel() {

    private var hasLoadedInitialData = false
    private var autoStopTriggered = false

    private var sessionStartedAtMs: Long? = null
    private var timerJob: Job? = null
    private var volumeBarIdCounter = 0L
    private var lastWaveUpdateMs = 0L
    private val waveUpdateIntervalMs = 120L

    private val _state = MutableStateFlow(PracticeSessionState(
        volumeBars = createInitialVolumeBars()
    ))
    val state = _state
        .onStart {
            if (!hasLoadedInitialData) {
                observeRecorder()
                observePlayer()
                hasLoadedInitialData = true
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = PracticeSessionState(
                volumeBars = createInitialVolumeBars()
            )
        )

    private fun createInitialVolumeBars(): List<VolumeBar> {
        return List(WAVE_BAR_COUNT) { index ->
            val fraction = (0.12f + 0.1f * abs(sin(index * 0.8f))).coerceIn(0.12f, 0.25f)
            VolumeBar(fraction, volumeBarIdCounter++)
        }
    }

    private val _event = Channel<PracticeSessionEvent>()
    val event: Flow<PracticeSessionEvent> = _event.receiveAsFlow()

    private fun observeRecorder() {
        viewModelScope.launch {
            voiceRecorder.recordingDetails.collect { details ->
                val wasRecording = _state.value.isRecording
                val isRecordingNow = details.isRecording

                _state.update {
                    val normalizedAmps = amplitudeNormalizer.remapAmplitudes(details.amplitudes)
                    val currentBars = it.volumeBars.toMutableList()
                    val currentTime = System.currentTimeMillis()
                    
                    if (normalizedAmps.isNotEmpty() && (currentTime - lastWaveUpdateMs >= waveUpdateIntervalMs)) {
                        val newAmp = normalizedAmps.last().coerceIn(0.12f, 1f)
                        currentBars.add(VolumeBar(newAmp, volumeBarIdCounter++))
                        if (currentBars.size > WAVE_BAR_COUNT) {
                            currentBars.removeAt(0)
                        }
                        lastWaveUpdateMs = currentTime
                    }

                    it.copy(
                        isRecording = details.isRecording,
                        recordedAudioPath = details.filePath,
                        amplitudes = normalizedAmps,
                        volumeBars = currentBars,
                        recordingDuration = details.duration
                    )
                }

                if (wasRecording && !isRecordingNow && !details.filePath.isNullOrBlank()) {
                    audioPlayer.prepare(details.filePath)
                }

                if (details.isRecording && details.duration >= 2.minutes && !autoStopTriggered) {
                    autoStopTriggered = true
                    onAction(StopRecordingAnswer)
                }
                if (!details.isRecording) {
                    autoStopTriggered = false
                }
            }
        }
        setupTtsListener()
    }

    private fun setupTtsListener() {
        textToSpeechManager.setOnUtteranceProgressListener(
            object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _state.update { it.copy(isReadingQuestion = true) }
                }

                override fun onDone(utteranceId: String?) {
                    _state.update { it.copy(isReadingQuestion = false) }
                }

                override fun onError(utteranceId: String?) {
                    _state.update { it.copy(isReadingQuestion = false) }
                }
            }
        )
    }

    private fun startSessionTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000.milliseconds)
                _state.update {
                    it.copy(totalSessionDuration = it.totalSessionDuration + 1.seconds)
                }
            }
        }
    }

    private fun observePlayer() {
        viewModelScope.launch {
            audioPlayer.activeTrack.collect { track ->
                _state.update {
                    it.copy(
                        isPlayingAudio = track.isPlaying,
                        playbackState = track.playbackState,
                        playbackPositionMs = track.durationPlayed.inWholeMilliseconds,
                        playbackDurationMs = track.totalDuration.inWholeMilliseconds
                    )
                }
            }
        }
    }

    fun onAction(action: PracticeSessionAction) {
        when (action) {
            is CreateNewPracticeSession -> createNewSession(action.trackId)

            is RestartPracticeSession -> restartOldSession(action.sessionId)

            is ShowOrHidePermissionDialog -> togglePermissionDialog(action.show)

            is ShowOrHideDiscardConfirmDialog -> toggleDiscardConfirmDialog(action.show)

            is ShowOrHideLeaveConfirmDialog -> toggleLeaveConfirmDialog(action.show)

            ListenToAIReadingCurrentQuestion -> readQuestion()

            PauseListeningToCurrentQuestion -> stopReadingQuestion()

            StopListeningToCurrentQuestionAndStartAnswering -> {
                stopReadingQuestion()
                startRecordingAnswer()
            }

            DiscardCurrentAnswer -> discardRecordedAnswer()

            StartRecordingAnswer -> startRecordingAnswer()

            ResumeRecordingAnswer -> resumeRecorder()

            PauseRecordingAnswer -> pauseRecorder()

            StopRecordingAnswer -> stopRecording()

            TogglePlayingCurrentRecordedAnswer -> togglePlayRecordedAnswer()

            is SeekAudioTo -> seekMediaPlayer(action.positionMs)

            SubmitAnswerToCurrentQuestion -> submitAnswer()

            ToggleQuestionCard -> toggleQuestionTextCard()

            is ShowOrHideSettingsBottomSheet -> toggleSettingsBottomSheet(action.show)

            is ToggleAutoReadQuestion -> toggleAutoReadQuestion(action.enabled)

            // Body language
            is AcceptBodyLanguageConsent -> acceptBodyLanguageConsent()
            is DeclineBodyLanguageConsent -> declineBodyLanguageConsent()
            is ToggleCameraPreview -> toggleCameraPreview(action.visible)
            is OnCameraPermissionResult -> handleCameraPermissionResult(action.granted)
            is OnCameraProviderReady -> startBodyLanguageAnalysis(
                action.cameraProvider, action.lifecycleOwner, action.surfaceProvider
            )
            is OnSurfaceProviderReady -> bodyLanguageAnalyzer.bindPreview(action.surfaceProvider)
        }
    }

    private fun toggleSettingsBottomSheet(show: Boolean) {
        _state.update { it.copy(showSettingsBottomSheet = show) }
    }

    private fun toggleAutoReadQuestion(enabled: Boolean) {
        _state.update { it.copy(autoReadQuestion = enabled) }
    }

    private fun stopRecording() {
        voiceRecorder.stop()
    }

    private fun toggleQuestionTextCard() {
        _state.update { it.copy(showQuestionCard = !it.showQuestionCard) }
    }

    private fun resumeRecorder() {
        voiceRecorder.resume()
    }

    private fun pauseRecorder() {
        voiceRecorder.pause()
    }

    private fun seekMediaPlayer(positionMs: Long) {
        stopReadingQuestion()
        audioPlayer.seekTo(positionMs)
    }

    private fun togglePlayRecordedAnswer() {
        val currentState = _state.value
        when (currentState.playbackState) {
            AudioPlaybackState.PLAYING -> audioPlayer.pause()
            AudioPlaybackState.PAUSED -> {
                stopReadingQuestion()
                audioPlayer.resume()
            }

            AudioPlaybackState.STOPPED -> {
                currentState.recordedAudioPath?.let { path ->
                    stopReadingQuestion()
                    audioPlayer.play(path) {
                        //todo add on complete if needed
                    }
                }
            }
        }
    }

    private fun discardRecordedAnswer() {
        voiceRecorder.cancel()
        _state.update {
            it.copy(
                recordedAudioPath = null,
                transcription = null,
                recordingDuration = Duration.ZERO,
                amplitudes = emptyList(),
                volumeBars = createInitialVolumeBars(),
                showDiscardConfirm = false
            )
        }
    }

    private fun startRecordingAnswer() {
        stopReadingQuestion()
        if (sessionStartedAtMs == null) {
            sessionStartedAtMs = System.currentTimeMillis()
            startSessionTimer()
        }
        voiceRecorder.start()
    }

    private fun stopReadingQuestion() {
        textToSpeechManager.stop()
        _state.update { it.copy(isReadingQuestion = false) }
    }

    private fun togglePermissionDialog(show: Boolean) {
        _state.update { it.copy(showPermissionDialog = show) }
    }

    private fun toggleDiscardConfirmDialog(show: Boolean) {
        _state.update { it.copy(showDiscardConfirm = show) }
    }

    private fun toggleLeaveConfirmDialog(show: Boolean) {
        _state.update { it.copy(showLeaveConfirm = show) }
    }

    private fun restartOldSession(sessionId: Long) {
        if (_state.value.currentSession != null || _state.value.isLoadingSession) return
        loadSession {
            sessionRepo.restartOldSession(sessionId)
        }
    }

    private fun createNewSession(trackId: Long) {
        if (_state.value.currentSession != null || _state.value.isLoadingSession) return
        loadSession {
            sessionRepo.createNewSession(
                CreateSessionRequest(
                    trackId = trackId,
                    questionCount = QUESTION_COUNT,
                    durationMinutes = SESSION_DURATION
                )
            )
        }
    }

    /**
     * Shared loader for both "create new session" and "resume existing session" —
     * these were previously two near-identical copies of the same success/error handling.
     */
    private fun loadSession(
        request: suspend () -> CareerPilotResult<Session, NetworkError>
    ) {
        viewModelScope.launch {
            _state.update { it.copy(isLoadingSession = true) }
            request()
                .onSuccess { session ->
                    handleSessionLoadSuccess(session)
                }
                .onError {
                    handleSessionLoadError(it)
                }
        }
    }

    private fun handleSessionLoadSuccess(session: Session) {
        _state.update {
            it.copy(
                isLoadingSession = false,
                currentSession = session,
                sessionId = session.sessionId,
                recordedAudioPath = null,
                transcription = null,
                recordingDuration = Duration.ZERO,
                amplitudes = emptyList(),
                volumeBars = createInitialVolumeBars(),
                showQuestionCard = true
            )
        }
        if (_state.value.autoReadQuestion) {
            readQuestion()
        }
        checkBodyLanguageAccess()
    }

    private suspend fun handleSessionLoadError(error: NetworkError) {
        _state.update { it.copy(isLoadingSession = false) }
        _event.send(PracticeSessionEvent.ShowError(error.toUIText()))
    }

    private fun readQuestion() {
        val question = _state.value.currentSession?.currentQuestion?.questionText
        if (!question.isNullOrBlank()) {
            _state.update { it.copy(showQuestionCard = true) }
            if (_state.value.isRecording) {
                voiceRecorder.stop()
            }
            audioPlayer.pause()
            textToSpeechManager.speak(question)
        }
    }

    private fun submitAnswer() {
        val audioPath = _state.value.recordedAudioPath ?: return
        val session = _state.value.currentSession ?: return
        val sessionId = session.sessionId

        stopReadingQuestion()
        voiceRecorder.stop()
        audioPlayer.stop()

        viewModelScope.launch {
            _state.update {
                it.copy(
                    isUploadingAndTranscribingAudio = true
                )
            }

            val uploadDeferred = async { uploadAudio(audioPath) }
            val transcribeDeferred = async { transcribeAudio(audioPath) }

            val uploadResult = uploadDeferred.await()
            val transcriptionResult = transcribeDeferred.await()

            transcriptionResult
                .onSuccess { transcript ->
                    _state.update { it.copy(transcription = transcript) }
                    uploadResult
                        .onSuccess { audioAttachment ->
                            uploadAnswer(sessionId, audioAttachment.url, transcript)
                        }
                        .onError { error ->
                            _state.update { it.copy(isUploadingAndTranscribingAudio = false) }
                            _event.send(PracticeSessionEvent.ShowError(error.toUIText()))
                        }
                }
                .onFailure { _ ->
                    _state.update { it.copy(isUploadingAndTranscribingAudio = false) }
                    _event.send(PracticeSessionEvent.ShowError(TranscriptionError.UNKNOWN.toUIText()))
                }
        }
    }

    private suspend fun uploadAudio(audioPath: String): CareerPilotResult<AudioAttachment, NetworkError> =
        sessionRepo.uploadAudio(File(audioPath)) { progress ->
            _state.update { it.copy(uploadProgress = progress) }
        }

    private suspend fun transcribeAudio(audioPath: String): Result<String> =
        withContext(Dispatchers.Default) {
            whisperEngine.transcribe(audioPath).onSuccess {
                Log.d("CareerPilot", "transcription success: $it")
            }
        }

    private suspend fun uploadAnswer(
        sessionId: Long,
        audioUrl: String,
        transcript: String
    ) {
        _state.update {
            it.copy(
                isUploadingAndTranscribingAudio = false,
                isSendingAnswer = true
            )
        }
        val elapsedSeconds = sessionStartedAtMs?.let {
            ((System.currentTimeMillis() - it) / 1000).toInt()
        } ?: 0

        sessionRepo.submitAnswer(
            sessionId = sessionId,
            request = AnswerRequest(
                transcript = transcript,
                audioUrl = audioUrl,
                durationMs = _state.value.recordingDuration.inWholeMilliseconds,
                sessionElapsedSeconds = elapsedSeconds,
                words = emptyList()
            )
        ).onSuccess { response ->
            handleAnswerSubmissionSuccess(
                sessionId,
                response
            )
        }.onError {
            _state.update {
                it.copy(
                    isSendingAnswer = false
                )
            }
            _event.send(PracticeSessionEvent.ShowError(it.toUIText()))
        }
    }

    private suspend fun handleAnswerSubmissionSuccess(
        sessionId: Long,
        answerResponse: AnswerResponse
    ) {
        if (answerResponse.sessionStatus.contains("READY_TO_COMPLETE", ignoreCase = true)) {
            // Finalize body language if running
            val metricsJson = if (bodyLanguageAnalyzer.isRunning) {
                bodyLanguageAnalyzer.stop()
                val metrics = bodyLanguageAnalyzer.finalizeSession()
                Json.encodeToString(BodyLanguageMetrics.serializer(), metrics)
            } else null

            _event.send(PracticeSessionEvent.NavigateToResult(sessionId, metricsJson))
            return
        }
        answerResponse.nextQuestion?.let {
            _state.update {
                it.copy(
                    isSendingAnswer = false,
                    currentSession = it.currentSession?.copy(
                        currentQuestion = answerResponse.nextQuestion
                    ),
                    recordedAudioPath = null,
                    transcription = null,
                    recordingDuration = Duration.ZERO,
                    amplitudes = emptyList(),
                    volumeBars = createInitialVolumeBars()
                )
            }
            if (_state.value.autoReadQuestion) {
                readQuestion()
            } else {
                _state.update {
                    it.copy(
                        showQuestionCard = true
                    )
                }
            }
        }
    }

    // region Body language

    private fun checkBodyLanguageAccess() {
        viewModelScope.launch {
            userProfileRepo.userProfile.first().let { profile ->
                val tier = profile.account.subscriptionTier.uppercase()
                val isPaid = tier in setOf("PLUS", "PRO", "MAX")
                val consentGiven = profile.account.bodyLanguageConsentGiven

                _state.update {
                    it.copy(
                        bodyLanguageEnabled = isPaid,
                        bodyLanguageConsentGiven = consentGiven,
                    )
                }

                if (isPaid && !consentGiven) {
                    _state.update { it.copy(showBodyLanguageConsentDialog = true) }
                } else if (isPaid && consentGiven) {
                    _event.send(PracticeSessionEvent.RequestCameraPermission)
                }
            }
        }
    }

    private fun acceptBodyLanguageConsent() {
        viewModelScope.launch {
            userProfileRepo.setBodyLanguageConsent(true)
            _state.update {
                it.copy(
                    bodyLanguageConsentGiven = true,
                    showBodyLanguageConsentDialog = false,
                )
            }
            _event.send(PracticeSessionEvent.RequestCameraPermission)
        }
    }

    private fun declineBodyLanguageConsent() {
        _state.update {
            it.copy(
                showBodyLanguageConsentDialog = false,
                bodyLanguageEnabled = false,
            )
        }
    }

    private fun handleCameraPermissionResult(granted: Boolean) {
        if (!granted) {
            _state.update { it.copy(bodyLanguageEnabled = false) }
        }
        // Camera provider will be obtained by the Composable and sent via OnCameraProviderReady
    }

    private fun startBodyLanguageAnalysis(
        cameraProvider: ProcessCameraProvider,
        lifecycleOwner: LifecycleOwner,
        surfaceProvider: Preview.SurfaceProvider?,
    ) {
        if (bodyLanguageAnalyzer.isRunning) return
        bodyLanguageAnalyzer.start(cameraProvider, lifecycleOwner, surfaceProvider)
        _state.update { it.copy(isBodyLanguageAnalyzing = true) }
    }

    private fun toggleCameraPreview(visible: Boolean) {
        _state.update { it.copy(isCameraPreviewVisible = visible) }
    }

    // endregion

    override fun onCleared() {
        textToSpeechManager.shutdown()
        voiceRecorder.cancel()
        audioPlayer.stop()
        bodyLanguageAnalyzer.stop()
    }
}