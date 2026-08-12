package com.iti.onboarding.presentation.screen.cv.viewmodel

import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iti.careerpilot.core.designsystem.components.CvUploadCardStage
import com.iti.common.error.NetworkError
import com.iti.common.media.pdfpicker.PdfOperations
import com.iti.common.result.CareerPilotResult
import com.iti.common.result.onError
import com.iti.common.result.onSuccess
import com.iti.common.util.toUIText
import com.iti.onboarding.domain.usecase.AnalyzeCvUseCase
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
    private val analyzeCv: AnalyzeCvUseCase,
    private val pdfReaderOperations: PdfOperations,
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
            UploadCvIntent.OnNextClick -> navigateAfterAnalysis()
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
        uploadJob.get()?.cancel()

        uploadJob.set(
            performFileUpload(
                uri = uri,
                uploadCall = { u, p -> analyzeCv(u, p) },
                onStart = {
                    _state.update {
                        it.copy(
                            stage = CvUploadCardStage.UPLOADING,
                            uploadProgress = 0,
                            selectedFile = null
                        )
                    }
                },
                onProgress = { progress ->
                    _state.update { currentState ->
                        currentState.copy(uploadProgress = progress)
                    }
                },
                onSuccess = { response ->
                    pdfReaderOperations.getPdfMetaData(uri).onSuccess { metadata ->
                        _state.update {
                            it.copy(
                                selectedFile = SelectedCvUiModel(
                                    fileId = response.id,
                                    name = metadata.name,
                                    sizeBytes = metadata.sizeBytes ?: 0L,
                                ),
                                stage = CvUploadCardStage.UPLOADED,
                                uploadProgress = 100,
                            )
                        }
                    }
                },
                onFinish = {
                    if (_state.value.stage != CvUploadCardStage.UPLOADED) {
                        _state.update {
                            it.copy(
                                stage = CvUploadCardStage.EMPTY,
                                uploadProgress = 0,
                            )
                        }
                    }
                }
            )
        )
    }
    private fun <T> performFileUpload(
        uri: Uri,
        uploadCall: suspend (Uri, (Int) -> Unit) -> CareerPilotResult<T, NetworkError>,
        onStart: () -> Unit,
        onProgress: (Int) -> Unit,
        onSuccess: suspend (T) -> Unit,
        onFinish: () -> Unit
    ): Job = viewModelScope.launch {
        val startTime = System.currentTimeMillis()
        var realProgress = 0
        var isDone = false
        var uploadResult: CareerPilotResult<T, NetworkError>? = null

        onStart()

        launch {
            uploadResult = uploadCall(uri) { realProgress = it }
            isDone = true
        }

        while (!isDone && realProgress == 0) delay(50.milliseconds)

        var displayProgress = 0
        while (true) {
            if (isDone && uploadResult is CareerPilotResult.Error) break
            val target =
                if (isDone && uploadResult is CareerPilotResult.Success) 100 else realProgress
            if (displayProgress < target) {
                displayProgress++
                onProgress(displayProgress)
            }
            if (displayProgress >= 100 && !isDone && _state.value.stage != CvUploadCardStage.PARSING) {
                _state.update { it.copy(stage = CvUploadCardStage.PARSING) }
            }
            if (isDone && displayProgress >= 100) break
            delay(20.milliseconds)
        }

        val elapsed = System.currentTimeMillis() - startTime
        uploadResult?.onSuccess { response ->
            if (elapsed < 2000) delay((2000 - elapsed).milliseconds)
            onSuccess(response)
        }?.onError { error ->
            _effects.emit(UploadCvEffect.ShowError(error.toUIText()))
        }
        onFinish()
    }

    private fun navigateAfterAnalysis() {
        if (_state.value.stage == CvUploadCardStage.UPLOADED) {
            viewModelScope.launch {
                _effects.emit(UploadCvEffect.NavigateNext)
            }
        } else if (selectedUriString != null && _state.value.stage != CvUploadCardStage.UPLOADING && _state.value.stage != CvUploadCardStage.PARSING) {
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
