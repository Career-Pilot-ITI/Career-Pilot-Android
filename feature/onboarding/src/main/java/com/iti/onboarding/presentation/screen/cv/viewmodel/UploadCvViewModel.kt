package com.iti.onboarding.presentation.screen.cv.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iti.common.result.CareerPilotResult
import com.iti.common.result.onError
import com.iti.common.result.onSuccess
import com.iti.common.util.toUIText
import com.iti.onboarding.domain.usecase.CompleteOnboardingUseCase
import com.iti.onboarding.domain.usecase.UploadCvUseCase
import com.iti.onboarding.presentation.screen.cv.state.CvUploadStage
import com.iti.onboarding.presentation.screen.cv.state.SelectedCvUiModel
import com.iti.onboarding.presentation.screen.cv.state.UploadCvEffect
import com.iti.onboarding.presentation.screen.cv.state.UploadCvIntent
import com.iti.onboarding.presentation.screen.cv.state.UploadCvUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds
import androidx.core.net.toUri

@HiltViewModel
class UploadCvViewModel @Inject constructor(
    private val uploadCv: UploadCvUseCase,
    private val completeOnboarding: CompleteOnboardingUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(UploadCvUiState())
    val state = _state.asStateFlow()

    private val _effects = MutableSharedFlow<UploadCvEffect>()
    val effects = _effects.asSharedFlow()

    private val uploadJob = AtomicReference<Job?>(null)

    fun onIntent(intent: UploadCvIntent) {
        when (intent) {
            UploadCvIntent.OnUploadAreaClick -> openPdfPicker()
            is UploadCvIntent.OnPdfSelected -> prepareAndUpload(intent.uri)
            UploadCvIntent.OnAnalyzeClick -> navigateAfterAnalysis()
            UploadCvIntent.OnSkipClick -> skipUpload()
        }
    }

    private fun openPdfPicker() {
        if (_state.value.isSubmitting) return

        viewModelScope.launch {
            _effects.emit(UploadCvEffect.OpenPdfPicker)
        }
    }

    private fun prepareAndUpload(uriString: String) {
        val uri = uriString.toUri()
        uploadJob.getAndSet(viewModelScope.launch {
            val startTime = System.currentTimeMillis()
            var realProgress = 0
            var isDone = false
            var uploadResult: CareerPilotResult<com.iti.onboarding.domain.model.UploadedFile, com.iti.common.error.NetworkError>? = null

            _state.update { it.copy(stage = CvUploadStage.UPLOADING, uploadProgress = 0f) }

            launch {
                uploadResult = uploadCv(uri) { realProgress = it }
                isDone = true
            }

            // Progress simulation like in EditProfile
            while (!isDone && realProgress == 0) delay(50.milliseconds)

            var displayProgress = 0
            while (true) {
                if (isDone && uploadResult is CareerPilotResult.Error) break
                val target = if (isDone && uploadResult is CareerPilotResult.Success) 100 else realProgress
                if (displayProgress < target) {
                    displayProgress++
                    _state.update { it.copy(uploadProgress = displayProgress / 100f) }
                }
                if (isDone && displayProgress >= 100) break
                delay(20.milliseconds)
            }

            val elapsed = System.currentTimeMillis() - startTime
            uploadResult?.onSuccess { response ->
                if (elapsed < 1000) delay((1000 - elapsed).milliseconds)
                _state.update {
                    it.copy(
                        selectedFile = SelectedCvUiModel(
                            fileId = response.id,
                            name = response.originalName,
                            sizeBytes = response.sizeBytes,
                        ),
                        stage = CvUploadStage.UPLOADED,
                        uploadProgress = 1f,
                    )
                }
            }?.onError { error ->
                _state.update { it.copy(stage = CvUploadStage.EMPTY, uploadProgress = 0f) }
                _effects.emit(UploadCvEffect.ShowError(error.toUIText()))
            }
        })?.cancel()
    }

    private fun navigateAfterAnalysis() {
        if (!_state.value.canAnalyze) return
        finishOnboarding(
            cvFileId = _state.value.selectedFile?.fileId,
            successEffect = UploadCvEffect.NavigateNext,
        )
    }

    private fun skipUpload() {
        uploadJob.get()?.cancel()

        finishOnboarding(
            cvFileId = null,
            successEffect = UploadCvEffect.Skip,
        )
    }

    private fun finishOnboarding(
        cvFileId: Long?,
        successEffect: UploadCvEffect,
    ) {
        viewModelScope.launch {
            val previousStage = _state.value.stage
            _state.update { it.copy(stage = CvUploadStage.PREPARING) }

            completeOnboarding(cvFileId)
                .onSuccess {
                    _effects.emit(successEffect)
                }
                .onError { error ->
                    _state.update { it.copy(stage = previousStage) }
                    _effects.emit(UploadCvEffect.ShowError(error.toUIText()))
                }
        }
    }
}
