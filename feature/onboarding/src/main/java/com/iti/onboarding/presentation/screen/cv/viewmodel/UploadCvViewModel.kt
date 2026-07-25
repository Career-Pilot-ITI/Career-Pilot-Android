package com.iti.onboarding.presentation.screen.cv.viewmodel

import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iti.common.result.CareerPilotResult
import com.iti.common.result.onError
import com.iti.common.result.onSuccess
import com.iti.common.util.toUIText
import com.iti.onboarding.domain.usecase.AnalyzeCvUseCase
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

@HiltViewModel
class UploadCvViewModel @Inject constructor(
    private val analyzeCv: AnalyzeCvUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(UploadCvUiState())
    val state = _state.asStateFlow()

    private val _effects = MutableSharedFlow<UploadCvEffect>()
    val effects = _effects.asSharedFlow()

    private val uploadJob = AtomicReference<Job?>(null)
    private var selectedUriString: String? = null

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
        selectedUriString = uriString
        val uri = uriString.toUri()
        uploadJob.getAndSet(viewModelScope.launch {
            val startTime = System.currentTimeMillis()
            var realProgress = 0
            var isDone = false
            var analyzeResult: CareerPilotResult<com.iti.core.datastore.models.UserProfile, com.iti.common.error.NetworkError>? = null

            _state.update { it.copy(stage = CvUploadStage.UPLOADING, uploadProgress = 0f) }

            launch {
                analyzeResult = analyzeCv(uri) { realProgress = it }
                isDone = true
            }

            while (!isDone && realProgress == 0) delay(50.milliseconds)

            var displayProgress = 0
            while (true) {
                if (isDone && analyzeResult is CareerPilotResult.Error) break
                val target = if (isDone && analyzeResult is CareerPilotResult.Success) 100 else realProgress
                if (displayProgress < target) {
                    displayProgress++
                    _state.update { it.copy(uploadProgress = displayProgress / 100f) }
                }
                if (isDone && displayProgress >= 100) break
                delay(20.milliseconds)
            }

            val elapsed = System.currentTimeMillis() - startTime
            analyzeResult?.onSuccess { response ->
                if (elapsed < 1000) delay((1000 - elapsed).milliseconds)
                val fileName = uri.lastPathSegment?.substringAfterLast('/') ?: ""
                _state.update {
                    it.copy(
                        selectedFile = SelectedCvUiModel(
                            fileId = response.id.toLong(),
                            name = fileName,
                            sizeBytes = 0L,
                        ),
                        stage = CvUploadStage.UPLOADED,
                        uploadProgress = 1f,
                    )
                }
                _effects.emit(UploadCvEffect.NavigateNext)
            }?.onError { error ->
                _state.update { it.copy(stage = CvUploadStage.EMPTY, uploadProgress = 0f) }
                _effects.emit(UploadCvEffect.ShowError(error.toUIText()))
            }
        })?.cancel()
    }

    private fun navigateAfterAnalysis() {
        if (_state.value.stage == CvUploadStage.UPLOADED) {
            viewModelScope.launch {
                _effects.emit(UploadCvEffect.NavigateNext)
            }
        } else if (selectedUriString != null && _state.value.stage != CvUploadStage.UPLOADING) {
            prepareAndUpload(selectedUriString!!)
        }
    }

    private fun skipUpload() {
        uploadJob.get()?.cancel()
        viewModelScope.launch {
            _effects.emit(UploadCvEffect.Skip)
        }
    }
}
