package com.iti.careerpilot.practicesession.presentation.practicescreen.viewmodel

import android.os.SystemClock
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iti.careerpilot.practicesession.domain.repo.SessionRepo
import com.iti.careerpilot.practicesession.presentation.practicescreen.action.PracticeSessionAction.*
import com.iti.careerpilot.practicesession.data.tts.TextToSpeechManager
import com.iti.careerpilot.practicesession.domain.audio.AudioPlayer
import com.iti.careerpilot.practicesession.domain.recording.RecordingAnswerClassifier
import com.iti.careerpilot.practicesession.domain.recording.VoiceRecorder
import com.iti.careerpilot.practicesession.presentation.practicescreen.event.PracticeSessionEvent
import com.iti.careerpilot.practicesession.presentation.practicescreen.state.PracticeSessionState
import com.iti.careerpilot.whisper.domain.WhisperEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import com.iti.careerpilot.practicesession.domain.models.CreateSessionRequest
import com.iti.careerpilot.practicesession.domain.models.AnswerRequest
import com.iti.careerpilot.practicesession.domain.models.AnswerResponse
import com.iti.careerpilot.practicesession.domain.models.AudioAttachment
import com.iti.common.result.CareerPilotResult
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import java.io.File
import java.time.Instant
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlin.time.Duration
import com.iti.careerpilot.practicesession.domain.audio.models.AudioPlaybackState
import com.iti.careerpilot.practicesession.domain.models.Session
import com.iti.careerpilot.practicesession.data.audio.AmplitudeNormalizer
import com.iti.careerpilot.practicesession.presentation.practicescreen.action.PracticeSessionAction
import com.iti.careerpilot.practicesession.presentation.practicescreen.state.VolumeBar
import com.iti.common.error.NetworkError
import com.iti.common.error.TranscriptionError
import com.iti.common.result.onError
import com.iti.common.result.onSuccess
import com.iti.common.util.toUIText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.math.sin
import kotlin.time.Duration.Companion.milliseconds

const val QUESTION_COUNT = 10
const val SESSION_DURATION = 20
const val WAVE_BAR_COUNT = 32

@HiltViewModel
class PracticeSessionViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val sessionRepo: SessionRepo,
    private val voiceRecorder: VoiceRecorder,
    private val audioPlayer: AudioPlayer,
    private val whisperEngine: WhisperEngine,
    private val textToSpeechManager: TextToSpeechManager,
    private val amplitudeNormalizer: AmplitudeNormalizer,
) : ViewModel() {

    private companion object {
        const val TAG = "PracticeSessionVM"
        const val KEY_ACTIVE_SESSION_ID = "practice_active_session_id"
        const val KEY_SESSION_STARTED_AT_MS = "practice_session_started_at_ms"
        const val MAX_RESTORABLE_SESSION_AGE_MS = 24L * 60L * 60L * 1000L
    }

    private var hasLoadedInitialData = false
    private var autoStopTriggered = false

    private var sessionStartedAtMs: Long?
        get() = savedStateHandle[KEY_SESSION_STARTED_AT_MS]
        set(value) {
            if (value == null) {
                savedStateHandle.remove<Long>(KEY_SESSION_STARTED_AT_MS)
            } else {
                savedStateHandle[KEY_SESSION_STARTED_AT_MS] = value
            }
        }
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
                        amplitudes = details.amplitudes,
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
                refreshSessionTimer()
                delay(1000.milliseconds)
            }
        }
    }

    private fun refreshSessionTimer() {
        val startedAt = sessionStartedAtMs ?: return
        val elapsedMs = (SystemClock.elapsedRealtime() - startedAt).coerceAtLeast(0L)
        _state.update { it.copy(totalSessionDuration = elapsedMs.milliseconds) }
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
                isEmptyAnswer = false,
                uploadProgress = 0,
                recordingDuration = Duration.ZERO,
                amplitudes = emptyList(),
                volumeBars = createInitialVolumeBars(),
                showDiscardConfirm = false
            )
        }
    }

    private fun startRecordingAnswer() {
        stopReadingQuestion()
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

        val restoredSessionId = savedStateHandle.get<Long>(KEY_ACTIVE_SESSION_ID)
        if (restoredSessionId != null && restoredSessionId > 0L) {
            restartOldSession(restoredSessionId)
            return
        }

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
        savedStateHandle[KEY_ACTIVE_SESSION_ID] = session.sessionId
        if (sessionStartedAtMs == null) {
            sessionStartedAtMs = resolveSessionStartMs(session.startedAt)
        }
        startSessionTimer()

        _state.update {
            it.copy(
                isLoadingSession = false,
                currentSession = session,
                sessionId = session.sessionId,
                recordedAudioPath = null,
                transcription = null,
                isEmptyAnswer = false,
                uploadProgress = 0,
                recordingDuration = Duration.ZERO,
                amplitudes = emptyList(),
                volumeBars = createInitialVolumeBars(),
                showQuestionCard = true
            )
        }
        refreshSessionTimer()
        if (_state.value.autoReadQuestion) {
            readQuestion()
        }
    }

    private fun resolveSessionStartMs(serverStartedAt: String): Long {
        val nowWallClock = System.currentTimeMillis()
        val nowElapsedRealtime = SystemClock.elapsedRealtime()
        val serverStart = runCatching { Instant.parse(serverStartedAt).toEpochMilli() }.getOrNull()
            ?: return nowElapsedRealtime
        val age = nowWallClock - serverStart

        return if (age in 0..MAX_RESTORABLE_SESSION_AGE_MS) {
            (nowElapsedRealtime - age).coerceAtLeast(0L)
        } else {
            nowElapsedRealtime
        }
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
        val isEmptyAnswer = RecordingAnswerClassifier.isLikelyEmpty(
            durationMs = _state.value.recordingDuration.inWholeMilliseconds,
            rawAmplitudes = _state.value.amplitudes,
        )

        stopReadingQuestion()
        voiceRecorder.stop()
        audioPlayer.stop()

        viewModelScope.launch {
            _state.update {
                it.copy(
                    isUploadingAndTranscribingAudio = true,
                    isEmptyAnswer = isEmptyAnswer,
                    uploadProgress = 0,
                )
            }

            val uploadDeferred = async { uploadAudio(audioPath) }
            val transcribeDeferred = if (isEmptyAnswer) {
                Log.d(TAG, "No meaningful speech detected; skipping local Whisper transcription")
                null
            } else {
                async { transcribeAudio(audioPath) }
            }

            val uploadResult = uploadDeferred.await()
            val transcriptionResult = transcribeDeferred?.await()
                ?: Result.success(RecordingAnswerClassifier.NO_ANSWER_TRANSCRIPT)

            transcriptionResult
                .onSuccess { transcript ->
                    val safeTranscript = transcript.ifBlank {
                        RecordingAnswerClassifier.NO_ANSWER_TRANSCRIPT
                    }
                    _state.update { it.copy(transcription = safeTranscript) }
                    uploadResult
                        .onSuccess { audioAttachment ->
                            uploadAnswer(sessionId, audioAttachment.url, safeTranscript)
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

    private suspend fun uploadAudio(audioPath: String): CareerPilotResult<AudioAttachment, NetworkError> {
        val file = File(audioPath)
        val startedAt = SystemClock.elapsedRealtime()
        val result = sessionRepo.uploadAudio(file) { progress ->
            _state.update { it.copy(uploadProgress = progress) }
        }
        Log.d(
            TAG,
            "Audio upload finished in ${SystemClock.elapsedRealtime() - startedAt} ms; size=${file.length()} bytes",
        )
        return result
    }

    private suspend fun transcribeAudio(audioPath: String): Result<String> {
        val startedAt = SystemClock.elapsedRealtime()
        val result = withContext(Dispatchers.Default) {
            whisperEngine.transcribe(audioPath)
        }
        Log.d(TAG, "Local transcription finished in ${SystemClock.elapsedRealtime() - startedAt} ms")
        return result.onSuccess {
            Log.d(TAG, "Transcription success: $it")
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
            ((SystemClock.elapsedRealtime() - it) / 1000).toInt()
        } ?: 0

        val submitStartedAt = SystemClock.elapsedRealtime()
        sessionRepo.submitAnswer(
            sessionId = sessionId,
            request = AnswerRequest(
                transcript = transcript,
                audioUrl = audioUrl,
                durationMs = _state.value.recordingDuration.inWholeMilliseconds,
                sessionElapsedSeconds = elapsedSeconds,
                words = emptyList()
            )
        ).also {
            Log.d(TAG, "Answer submit finished in ${SystemClock.elapsedRealtime() - submitStartedAt} ms")
        }.onSuccess { response ->
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
            timerJob?.cancel()
            savedStateHandle.remove<Long>(KEY_ACTIVE_SESSION_ID)
            sessionStartedAtMs = null
            _event.send(PracticeSessionEvent.NavigateToResult(sessionId))
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
                    isEmptyAnswer = false,
                    uploadProgress = 0,
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

    override fun onCleared() {
        textToSpeechManager.shutdown()
        voiceRecorder.cancel()
        audioPlayer.stop()
    }
}