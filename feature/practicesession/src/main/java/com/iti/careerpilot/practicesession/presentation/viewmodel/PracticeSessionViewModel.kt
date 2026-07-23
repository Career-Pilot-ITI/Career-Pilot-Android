package com.iti.careerpilot.practicesession.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iti.careerpilot.practicesession.domain.repo.SessionRepo
import com.iti.careerpilot.practicesession.presentation.action.PracticeSessionAction
import com.iti.careerpilot.practicesession.data.tts.TextToSpeechManager
import com.iti.careerpilot.practicesession.domain.audio.AudioPlayer
import com.iti.careerpilot.practicesession.domain.recording.VoiceRecorder
import com.iti.careerpilot.practicesession.presentation.event.PracticeSessionEvent
import com.iti.careerpilot.practicesession.presentation.state.PracticeSessionState
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
import com.iti.common.result.CareerPilotResult
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import java.io.File
import kotlin.time.Duration.Companion.minutes
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import com.iti.careerpilot.practicesession.domain.models.Session
import com.iti.common.error.NetworkError
import com.iti.common.util.toUIText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class PracticeSessionViewModel @Inject constructor(
    private val sessionRepo: SessionRepo,
    private val voiceRecorder: VoiceRecorder,
    private val audioPlayer: AudioPlayer,
    private val whisperEngine: WhisperEngine,
    private val textToSpeechManager: TextToSpeechManager,
) : ViewModel() {

    private var hasLoadedInitialData = false
    private var autoStopTriggered = false

    private var sessionStartedAtMs: Long? = null
    private var timerJob: Job? = null

    private val _state = MutableStateFlow(PracticeSessionState())
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
            initialValue = PracticeSessionState()
        )

    private val _event = Channel<PracticeSessionEvent>()
    val event: Flow<PracticeSessionEvent> = _event.receiveAsFlow()

    private fun observeRecorder() {
        viewModelScope.launch {
            voiceRecorder.recordingDetails.collect { details ->
                _state.update {
                    it.copy(
                        isRecording = details.isRecording,
                        recordedAudioPath = details.filePath,
                        amplitudes = details.amplitudes,
                        recordingDuration = details.duration
                    )
                }

                if (details.isRecording && details.duration >= 2.minutes && !autoStopTriggered) {
                    autoStopTriggered = true
                    onAction(PracticeSessionAction.FinishRecordingAnswerAndStartTranscription)
                }
                if (!details.isRecording) {
                    autoStopTriggered = false
                }
            }
        }
        setupTtsListener()
    }

    private fun setupTtsListener() {
        textToSpeechManager.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                _state.update { it.copy(isReadingQuestion = true) }
            }

            override fun onDone(utteranceId: String?) {
                _state.update { it.copy(isReadingQuestion = false) }
            }

            override fun onError(utteranceId: String?) {
                _state.update { it.copy(isReadingQuestion = false) }
            }
        })
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
                        playbackPositionMs = track.durationPlayed.inWholeMilliseconds,
                        playbackDurationMs = track.totalDuration.inWholeMilliseconds
                    )
                }
            }
        }
    }

    fun onAction(action: PracticeSessionAction) {
        when (action) {
            is PracticeSessionAction.CreateNewPracticeSession -> createNewSession(action)

            is PracticeSessionAction.RestartPracticeSession -> restartOldSession(action)

            is PracticeSessionAction.ShowOrHidePermissionDialog -> togglePermissionDialog(action)

            PracticeSessionAction.ListenToAIReadingCurrentQuestion -> readQuestion()

            PracticeSessionAction.PauseListeningToCurrentQuestion -> stopReadingQuestion()

            PracticeSessionAction.StopListeningToCurrentQuestionAndStartAnswering -> {
                stopReadingQuestion()
                startRecordingAnswer()
            }

            PracticeSessionAction.DiscardCurrentAnswer -> discardRecordedAnswer()

            PracticeSessionAction.FinishRecordingAnswerAndStartTranscription -> voiceRecorder.stop()

            PracticeSessionAction.PauseRecordingAnswer -> pauseRecorder()

            PracticeSessionAction.PlayCurrentRecordedAnswer -> playRecordedAnswer()

            is PracticeSessionAction.SeekAudioTo -> seekMediaPlayer(action)

            PracticeSessionAction.ResumeRecordingAnswer -> resumeRecorder()

            PracticeSessionAction.StartRecordingAnswer -> startRecordingAnswer()

            PracticeSessionAction.SubmitFinalAnswerToCurrentQuestion -> submitAnswer()

            PracticeSessionAction.SkipCurrentQuestion -> skipCurrentQuestion()

            PracticeSessionAction.ToggleQuestionCard -> toggleQuestionTextCard()
        }
    }

    private fun toggleQuestionTextCard() {
        _state.update { it.copy(showQuestionCard = !it.showQuestionCard) }
    }

    private fun skipCurrentQuestion() {
        val sessionId = _state.value.sessionId
        if (sessionId != 0L) {
            loadSession { sessionRepo.getSessionState(sessionId) }
        }
    }

    private fun resumeRecorder() {
        voiceRecorder.resume()
    }

    private fun pauseRecorder() {
        voiceRecorder.pause()
    }

    private fun seekMediaPlayer(action: PracticeSessionAction.SeekAudioTo) {
        audioPlayer.seekTo(action.positionMs)
    }

    private fun playRecordedAnswer() {
        val path = _state.value.recordedAudioPath
        if (path != null) {
            audioPlayer.play(path) {
                _state.update { it.copy(isPlayingAudio = false) }
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
                amplitudes = emptyList()
            )
        }
    }

    private fun startRecordingAnswer() {
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

    private fun togglePermissionDialog(action: PracticeSessionAction.ShowOrHidePermissionDialog) {
        _state.update { it.copy(showPermissionDialog = action.show) }
    }

    private fun restartOldSession(action: PracticeSessionAction.RestartPracticeSession) {
        loadSession(resetAnswerState = false) {
            sessionRepo.getSessionState(action.sessionId)
        }
    }

    private fun createNewSession(action: PracticeSessionAction.CreateNewPracticeSession) {
        loadSession {
            sessionRepo.createNewSession(
                CreateSessionRequest(
                    trackId = action.trackId,
                    questionCount = 5,
                    durationMinutes = 15
                )
            )
        }
    }

    /**
     * Shared loader for both "create new session" and "resume existing session" —
     * these were previously two near-identical copies of the same success/error handling.
     */
    private fun loadSession(
        resetAnswerState: Boolean = true,
        request: suspend () -> CareerPilotResult<Session, NetworkError>
    ) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            when (val result = request()) {
                is CareerPilotResult.Success -> {
                    _state.update {
                        val base = it.copy(
                            isLoading = false,
                            currentSession = result.data,
                            sessionId = result.data.sessionId
                        )
                        if (resetAnswerState) {
                            readQuestion()
                            base.copy(
                                recordedAudioPath = null,
                                transcription = null,
                                recordingDuration = Duration.ZERO,
                                amplitudes = emptyList()
                            )
                        } else base
                    }
                }
                is CareerPilotResult.Error -> {
                    _state.update { it.copy(isLoading = false) }
                    _event.send(PracticeSessionEvent.ShowError(result.error.toUIText()))
                }
            }
        }
    }

    private fun readQuestion() {
        val question = _state.value.currentSession?.currentQuestion?.questionText
        if (!question.isNullOrBlank()) {
            textToSpeechManager.speak(question)
        }
    }

    private fun submitAnswer() {
        val audioPath = _state.value.recordedAudioPath ?: return
        val session = _state.value.currentSession ?: return
        val sessionId = session.sessionId

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, isUploading = true, isTranscribing = true) }

            val audioFile = File(audioPath)

            val uploadDeferred = async {
                sessionRepo.uploadAudio(audioFile) { progress ->
                    _state.update { it.copy(uploadProgress = progress) }
                }
            }

            val transcribeDeferred = async {
                withContext(Dispatchers.Default) {
                    val pcmData = decodeAudio(audioPath)
                    if (pcmData != null) {
                        whisperEngine.transcribe(pcmData)
                    } else {
                        Result.failure(IllegalStateException("Failed to decode recorded audio"))
                    }
                }
            }

            val uploadResult = uploadDeferred.await()
            val transcriptionResult = transcribeDeferred.await()

            _state.update { it.copy(isUploading = false, isTranscribing = false) }

            if (uploadResult is CareerPilotResult.Success && transcriptionResult.isSuccess) {
                val transcript = transcriptionResult.getOrNull().orEmpty()
                val audioUrl = uploadResult.data.url
                val elapsedSeconds = sessionStartedAtMs?.let {
                    ((System.currentTimeMillis() - it) / 1000).toInt()
                } ?: 0

                val answerResult = sessionRepo.submitAnswer(
                    sessionId = sessionId,
                    request = AnswerRequest(
                        transcript = transcript,
                        audioUrl = audioUrl,
                        durationMs = _state.value.recordingDuration.inWholeMilliseconds.toInt(),
                        sessionElapsedSeconds = elapsedSeconds,
                        words = emptyList() // TODO
                    )
                )

                when (answerResult) {
                    is CareerPilotResult.Success -> {
                        val answerResponse = answerResult.data
                        _state.update { it.copy(transcription = transcript) }
                        if (answerResponse.sessionStatus.contains("READY_TO_COMPLETE", ignoreCase = true)) {
                            _state.update { it.copy(isFinished = true, isLoading = false) }
                            _event.send(PracticeSessionEvent.NavigateToResult(sessionId))
                        } else if (answerResponse.nextQuestion != null) {
                            loadSession { sessionRepo.getSessionState(sessionId) }
                        } else {
                            _state.update { it.copy(isLoading = false) }
                        }
                    }
                    is CareerPilotResult.Error -> {
                        _state.update { it.copy(isLoading = false) }
                        _event.send(PracticeSessionEvent.ShowError(answerResult.error.toUIText()))
                    }
                }
            } else {
                _state.update { it.copy(isLoading = false) }
                val message = when {
                    uploadResult is CareerPilotResult.Error -> uploadResult.error.toUIText()
                    transcriptionResult.isFailure -> NetworkError.UNKNOWN.toUIText()
                    else -> NetworkError.UNKNOWN.toUIText()
                }
                _event.send(PracticeSessionEvent.ShowError(message))
            }
        }
    }

    /**
     * Runs on Dispatchers.Default (see caller). Decodes the recorded file to PCM float
     * samples for the on-device Whisper model.
     */
    private fun decodeAudio(filePath: String): FloatArray? {
        val extractor = MediaExtractor()
        try {
            extractor.setDataSource(filePath)
            val trackIndex = (0 until extractor.trackCount).firstOrNull {
                extractor.getTrackFormat(it).getString(MediaFormat.KEY_MIME)?.startsWith("audio/") == true
            } ?: return null

            extractor.selectTrack(trackIndex)
            val format = extractor.getTrackFormat(trackIndex)
            val codec = MediaCodec.createDecoderByType(format.getString(MediaFormat.KEY_MIME)!!)
            codec.configure(format, null, null, 0)
            codec.start()

            val info = MediaCodec.BufferInfo()
            var buffer = FloatArray(1 shl 16)
            var count = 0
            fun append(samples: ShortArray) {
                if (count + samples.size > buffer.size) {
                    buffer = buffer.copyOf(maxOf(buffer.size * 2, count + samples.size))
                }
                for (s in samples) buffer[count++] = s / 32768f
            }

            var isEOS = false
            while (!isEOS) {
                val inputIndex = codec.dequeueInputBuffer(10_000)
                if (inputIndex >= 0) {
                    val inputBuffer = codec.getInputBuffer(inputIndex)!!
                    val sampleSize = extractor.readSampleData(inputBuffer, 0)
                    if (sampleSize < 0) {
                        codec.queueInputBuffer(inputIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                        isEOS = true
                    } else {
                        codec.queueInputBuffer(inputIndex, 0, sampleSize, extractor.sampleTime, 0)
                        extractor.advance()
                    }
                }

                var outputIndex = codec.dequeueOutputBuffer(info, 10_000)
                while (outputIndex >= 0) {
                    val outputBuffer = codec.getOutputBuffer(outputIndex)!!
                    val samples = ShortArray(info.size / 2)
                    outputBuffer.asShortBuffer().get(samples)
                    append(samples)
                    codec.releaseOutputBuffer(outputIndex, false)
                    outputIndex = codec.dequeueOutputBuffer(info, 10_000)
                }
            }
            codec.stop()
            codec.release()
            extractor.release()
            return buffer.copyOf(count)
        } catch (e: Exception) {
            Log.e("PracticeSessionVM", "Failed to decode audio at $filePath", e)
            return null
        } finally {
            extractor.release()
        }
    }

    override fun onCleared() {
        textToSpeechManager.shutdown()
        voiceRecorder.cancel()
        audioPlayer.stop()
    }
}