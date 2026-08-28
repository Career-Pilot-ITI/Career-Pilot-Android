package com.iti.careerpilot.companyinterview.presentation.mvi

import com.iti.careerpilot.companyinterview.domain.models.ApplicantQuestion
import com.iti.careerpilot.companyinterview.domain.models.CompanyInterviewMetadata

sealed interface CompanyInterviewStep {
    data object Loading : CompanyInterviewStep
    data object MetadataOverview : CompanyInterviewStep
    data object EmailVerificationRequired : CompanyInterviewStep
    data object PermissionCheck : CompanyInterviewStep
    data class ActiveInterview(
        val question: ApplicantQuestion,
        val remainingSeconds: Int,
        val isRecording: Boolean,
        val currentTranscript: String = "",
        val eyeContactScore: Float = 100f,
        val postureScore: Float = 100f
    ) : CompanyInterviewStep
    data object SubmittingAnswer : CompanyInterviewStep
    data object CompletedSuccess : CompanyInterviewStep
    data class Error(val message: String) : CompanyInterviewStep
}

data class CompanyInterviewUiState(
    val step: CompanyInterviewStep = CompanyInterviewStep.Loading,
    val metadata: CompanyInterviewMetadata? = null,
    val token: String = "",
    val verifiedEmail: String = "",
    val totalQuestions: Int = 0,
    val currentQuestionIndex: Int = 0,
    val errorMessage: String? = null,
    val appExitCount: Int = 0
)

sealed interface CompanyInterviewUiIntent {
    data class LoadMetadata(val token: String) : CompanyInterviewUiIntent
    data class VerifyEmail(val email: String) : CompanyInterviewUiIntent
    data object ConfirmRulesAndStart : CompanyInterviewUiIntent
    data object StartRecordingAnswer : CompanyInterviewUiIntent
    data object StopAndSubmitAnswer : CompanyInterviewUiIntent
    data class UpdateTranscript(val transcript: String) : CompanyInterviewUiIntent
    data class UpdateVisionScores(val eyeContact: Float, val posture: Float) : CompanyInterviewUiIntent
    data object TimerTick : CompanyInterviewUiIntent
    data object AppSwitchedBackground : CompanyInterviewUiIntent
    data object DismissError : CompanyInterviewUiIntent
    data object ExitInterview : CompanyInterviewUiIntent
}

sealed interface CompanyInterviewUiEffect {
    data object NavigateHome : CompanyInterviewUiEffect
    data class ShowToast(val message: String) : CompanyInterviewUiEffect
    data object RequestCameraAndMicPermissions : CompanyInterviewUiEffect
    data object VibrateHapticCountdown : CompanyInterviewUiEffect
}
