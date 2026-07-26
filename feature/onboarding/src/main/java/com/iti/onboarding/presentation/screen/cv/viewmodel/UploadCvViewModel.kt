package com.iti.onboarding.presentation.screen.cv.viewmodel

import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iti.common.media.pdfpicker.PdfOperations
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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject

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

        uploadJob.set(viewModelScope.launch {
            _state.update {
                it.copy(
                    stage = CvUploadStage.UPLOADING,
                    uploadProgress = 0f,
                    selectedFile = null
                )
            }

            val analyzeResult = analyzeCv(uri) { progress ->
                val normalizedProgress = (progress / 100f)
                    .coerceIn(0f, 1f)

                _state.update { currentState ->
                    currentState.copy(
                        uploadProgress = normalizedProgress,
                    )
                }
            }

            analyzeResult
                .onSuccess { response ->
                    pdfReaderOperations.getPdfMetaData(uri).onSuccess { metadata ->
                        _state.update {
                            it.copy(
                                selectedFile = SelectedCvUiModel(
                                    fileId = response.id.toLong(),
                                    name = metadata.name,
                                    sizeBytes = metadata.sizeBytes ?: 0L,
                                ),
                                stage = CvUploadStage.UPLOADED,
                                uploadProgress = 1f,
                            )
                        }
                    }
                }
                .onError { error ->
                    _state.update {
                        it.copy(
                            stage = CvUploadStage.EMPTY,
                            uploadProgress = 0f,
                        )
                    }

                    _effects.emit(
                        UploadCvEffect.ShowError(error.toUIText())
                    )
                }
        })
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
