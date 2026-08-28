package com.iti.careerpilot.companyinterview.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iti.careerpilot.bodylanguage.BodyLanguageAnalyzer
import com.iti.careerpilot.companyinterview.domain.models.ApplicantQuestion
import com.iti.careerpilot.companyinterview.domain.usecases.*
import com.iti.careerpilot.companyinterview.presentation.mvi.*
import com.iti.careerpilot.whisper.domain.WhisperEngine
import com.iti.common.dispatcher.CareerPilotDispatchers.IO
import com.iti.common.dispatcher.Dispatcher
import com.iti.common.result.onError
import com.iti.common.result.onSuccess
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CompanyInterviewViewModel @Inject constructor(
    private val getMetadataUseCase: GetCompanyInterviewMetadataUseCase,
    private val verifyEmailUseCase: VerifyApplicantEmailUseCase,
    private val startSessionUseCase: StartCompanyInterviewSessionUseCase,
    private val submitAnswerUseCase: SubmitCompanyInterviewAnswerUseCase,
    private val completeSessionUseCase: CompleteCompanyInterviewSessionUseCase,
    private val recoverSessionUseCase: RecoverCompanyInterviewSessionUseCase,
    val bodyLanguageAnalyzer: BodyLanguageAnalyzer,
    private val whisperEngine: WhisperEngine,
    @Dispatcher(IO) private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _uiState = MutableStateFlow(CompanyInterviewUiState())
    val uiState: StateFlow<CompanyInterviewUiState> = _uiState.asStateFlow()

    private val _effectChannel = Channel<CompanyInterviewUiEffect>()
    val effect = _effectChannel.receiveAsFlow()

    private var timerJob: Job? = null
    private var currentSessionId: Long? = null
    private var recordedAudioPath: String? = null

    fun onIntent(intent: CompanyInterviewUiIntent) {
        when (intent) {
            is CompanyInterviewUiIntent.LoadMetadata -> handleLoadMetadata(intent.token)
            is CompanyInterviewUiIntent.VerifyEmail -> handleVerifyEmail(intent.email)
            is CompanyInterviewUiIntent.ConfirmRulesAndStart -> handleStartSession()
            is CompanyInterviewUiIntent.StartRecordingAnswer -> handleStartRecording()
            is CompanyInterviewUiIntent.StopAndSubmitAnswer -> handleStopAndSubmit()
            is CompanyInterviewUiIntent.UpdateTranscript -> handleUpdateTranscript(intent.transcript)
            is CompanyInterviewUiIntent.UpdateVisionScores -> handleUpdateVision(intent.eyeContact, intent.posture)
            is CompanyInterviewUiIntent.TimerTick -> handleTimerTick()
            is CompanyInterviewUiIntent.AppSwitchedBackground -> handleAppExit()
            is CompanyInterviewUiIntent.DismissError -> _uiState.update { it.copy(errorMessage = null) }
            is CompanyInterviewUiIntent.ExitInterview -> {
                bodyLanguageAnalyzer.stop()
                viewModelScope.launch { _effectChannel.send(CompanyInterviewUiEffect.NavigateHome) }
            }
        }
    }

    private fun handleLoadMetadata(token: String) {
        _uiState.update { it.copy(step = CompanyInterviewStep.Loading, token = token) }

        viewModelScope.launch(ioDispatcher) {
            getMetadataUseCase(token)
                .onSuccess { meta ->
                    val nextStep = if (meta.sessionCompleted) {
                        CompanyInterviewStep.CompletedSuccess
                    } else if (!meta.emailVerified) {
                        CompanyInterviewStep.EmailVerificationRequired
                    } else {
                        CompanyInterviewStep.MetadataOverview
                    }

                    _uiState.update {
                        it.copy(
                            step = nextStep,
                            metadata = meta,
                            verifiedEmail = meta.applicantEmail,
                            totalQuestions = meta.totalQuestions
                        )
                    }
                }
                .onError { error ->
                    _uiState.update {
                        it.copy(
                            step = CompanyInterviewStep.Error("Could not load interview details: ${error.name}"),
                            errorMessage = error.name
                        )
                    }
                }
        }
    }

    private fun handleVerifyEmail(email: String) {
        val token = _uiState.value.token
        if (token.isBlank()) return

        _uiState.update { it.copy(step = CompanyInterviewStep.Loading) }

        viewModelScope.launch(ioDispatcher) {
            verifyEmailUseCase(token, email)
                .onSuccess { meta ->
                    _uiState.update {
                        it.copy(
                            step = CompanyInterviewStep.MetadataOverview,
                            metadata = meta,
                            verifiedEmail = email
                        )
                    }
                }
                .onError { error ->
                    _uiState.update {
                        it.copy(
                            step = CompanyInterviewStep.EmailVerificationRequired,
                            errorMessage = "Email does not match invitation. Please verify and try again."
                        )
                    }
                }
        }
    }

    private fun handleStartSession() {
        val token = _uiState.value.token
        _uiState.update { it.copy(step = CompanyInterviewStep.Loading) }

        viewModelScope.launch(ioDispatcher) {
            startSessionUseCase(token)
                .onSuccess { sessionState ->
                    currentSessionId = sessionState.sessionId
                    bodyLanguageAnalyzer.start()

                    val q = sessionState.currentQuestion
                    if (q != null) {
                        _uiState.update {
                            it.copy(
                                step = CompanyInterviewStep.ActiveInterview(
                                    question = q,
                                    remainingSeconds = q.timeLimitSeconds,
                                    isRecording = true
                                ),
                                totalQuestions = sessionState.totalQuestions,
                                currentQuestionIndex = q.questionOrder
                            )
                        }
                        bodyLanguageAnalyzer.setRecordingActive(true)
                        startCountdownTimer(q.timeLimitSeconds)
                    } else {
                        _uiState.update { it.copy(step = CompanyInterviewStep.CompletedSuccess) }
                    }
                }
                .onError { error ->
                    _uiState.update {
                        it.copy(
                            step = CompanyInterviewStep.Error(error.name),
                            errorMessage = "Could not start interview session."
                        )
                    }
                }
        }
    }

    private fun startCountdownTimer(totalSeconds: Int) {
        timerJob?.cancel()
        timerJob = viewModelScope.launch(ioDispatcher) {
            var remaining = totalSeconds
            while (remaining > 0) {
                delay(1000)
                remaining--
                val current = _uiState.value.step
                if (current is CompanyInterviewStep.ActiveInterview) {
                    _uiState.update {
                        it.copy(step = current.copy(remainingSeconds = remaining))
                    }
                    if (remaining == 5) {
                        _effectChannel.send(CompanyInterviewUiEffect.VibrateHapticCountdown)
                    }
                }
            }
            // Auto-submit when time expires
            handleStopAndSubmit()
        }
    }

    private fun handleStartRecording() {
        bodyLanguageAnalyzer.setRecordingActive(true)
        val current = _uiState.value.step
        if (current is CompanyInterviewStep.ActiveInterview) {
            _uiState.update { it.copy(step = current.copy(isRecording = true)) }
        }
    }

    private fun handleStopAndSubmit() {
        timerJob?.cancel()
        bodyLanguageAnalyzer.setRecordingActive(false)

        val current = _uiState.value.step as? CompanyInterviewStep.ActiveInterview ?: return
        _uiState.update { it.copy(step = CompanyInterviewStep.SubmittingAnswer) }

        val token = _uiState.value.token
        val question = current.question
        val durationSpent = question.timeLimitSeconds - current.remainingSeconds

        viewModelScope.launch(ioDispatcher) {
            val blMetrics = runCatching { bodyLanguageAnalyzer.finalizeSession() }.getOrNull()
            val blMap = mutableMapOf<String, String>()
            if (blMetrics != null) {
                val postureScore = (100f - blMetrics.slouchPercentage).coerceIn(0f, 100f)
                val eyeScore = blMetrics.eyeContactPercentage.coerceIn(0f, 100f)
                val overallVision = ((eyeScore * 0.5f) + (postureScore * 0.5f)).coerceIn(0f, 100f)
                blMap["overallScore"] = overallVision.toString()
                blMap["eyeContactScore"] = eyeScore.toString()
                blMap["postureScore"] = postureScore.toString()
            }

            val proctoringMap = mapOf(
                "appExitCount" to _uiState.value.appExitCount.toString()
            )

            submitAnswerUseCase(
                token = token,
                questionId = question.questionId,
                transcript = current.currentTranscript,
                durationSeconds = durationSpent,
                speechRateWpm = 120.0,
                avgPauseMs = 400.0,
                silenceRatio = 0.15,
                fillerWordCount = 0,
                bodyLanguageMetrics = blMap,
                proctoringFlags = proctoringMap
            ).onSuccess { nextQuestion ->
                if (nextQuestion != null) {
                    _uiState.update {
                        it.copy(
                            step = CompanyInterviewStep.ActiveInterview(
                                question = nextQuestion,
                                remainingSeconds = nextQuestion.timeLimitSeconds,
                                isRecording = true
                            ),
                            currentQuestionIndex = nextQuestion.questionOrder
                        )
                    }
                    bodyLanguageAnalyzer.setRecordingActive(true)
                    startCountdownTimer(nextQuestion.timeLimitSeconds)
                } else {
                    // All questions completed
                    completeSessionUseCase(token)
                    bodyLanguageAnalyzer.stop()
                    _uiState.update { it.copy(step = CompanyInterviewStep.CompletedSuccess) }
                }
            }.onError { error ->
                _uiState.update {
                    it.copy(
                        step = CompanyInterviewStep.Error("Submission failed: ${error.name}"),
                        errorMessage = "Could not save your answer. Please retry."
                    )
                }
            }
        }
    }

    private fun handleUpdateTranscript(text: String) {
        val current = _uiState.value.step
        if (current is CompanyInterviewStep.ActiveInterview) {
            _uiState.update { it.copy(step = current.copy(currentTranscript = text)) }
        }
    }

    private fun handleUpdateVision(eyeContact: Float, posture: Float) {
        val current = _uiState.value.step
        if (current is CompanyInterviewStep.ActiveInterview) {
            _uiState.update {
                it.copy(step = current.copy(eyeContactScore = eyeContact, postureScore = posture))
            }
        }
    }

    private fun handleTimerTick() {
        // Handled in timer loop
    }

    private fun handleAppExit() {
        _uiState.update { it.copy(appExitCount = it.appExitCount + 1) }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        bodyLanguageAnalyzer.stop()
    }
}
