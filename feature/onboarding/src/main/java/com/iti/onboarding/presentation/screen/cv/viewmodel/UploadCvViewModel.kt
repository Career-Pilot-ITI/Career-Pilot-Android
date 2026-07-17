package com.iti.onboarding.presentation.screen.cv.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iti.common.media.pdfpicker.PdfReader
import com.iti.common.result.CareerPilotResult
import com.iti.common.util.toUIText
import com.iti.core.model.PdfFile
import com.iti.onboarding.domain.usecase.UploadCvUseCase
import com.iti.onboarding.presentation.screen.cv.state.CvUploadStage
import com.iti.onboarding.presentation.screen.cv.state.SelectedCvUiModel
import com.iti.onboarding.presentation.screen.cv.state.UploadCvEffect
import com.iti.onboarding.presentation.screen.cv.state.UploadCvIntent
import com.iti.onboarding.presentation.screen.cv.state.UploadCvUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class UploadCvViewModel @Inject constructor(
    private val uploadCv: UploadCvUseCase,
    private val pdfReader: PdfReader
) : ViewModel() {

    private val _state = MutableStateFlow(UploadCvUiState())
    val state = _state.asStateFlow()

    private val _effects = MutableSharedFlow<UploadCvEffect>()
    val effects = _effects.asSharedFlow()

    private var uploadJob: Job? = null

    fun onIntent(intent: UploadCvIntent) {
        when (intent) {
            UploadCvIntent.OnUploadAreaClick -> openPdfPicker()
            is UploadCvIntent.OnPdfSelected -> prepareAndUpload(intent.uri)
            UploadCvIntent.OnAnalyzeClick -> navigateAfterAnalysis()
            UploadCvIntent.OnSkipClick -> skipUpload()
        }
    }

    private fun openPdfPicker() {
        if (_state.value.isBusy) return

        viewModelScope.launch {
            _effects.emit(UploadCvEffect.OpenPdfPicker)
        }
    }

    private fun prepareAndUpload(uri: String) {
        uploadJob?.cancel()
        uploadJob = viewModelScope.launch {
            _state.value = UploadCvUiState(
                stage = CvUploadStage.PREPARING,
            )

            when (val result = pdfReader.readPdf(uri)) {
                is CareerPilotResult.Error -> {
                    _state.value = UploadCvUiState()

                    _effects.emit(
                        UploadCvEffect.ShowError(
                            result.error.toUIText(),
                        )
                    )
                }

                is CareerPilotResult.Success -> {
                    _state.update {
                        it.copy(
                            selectedFile = SelectedCvUiModel(
                                name = result.data.name,
                                sizeBytes = result.data.sizeBytes,
                            ),
                            stage = CvUploadStage.SELECTED,
                            uploadProgress = 0f,
                        )
                    }

                    uploadSelectedDocument(result.data)
                }
            }
        }
    }

    private suspend fun uploadSelectedDocument(document: PdfFile) {
        _state.update {
            it.copy(
                stage = CvUploadStage.UPLOADING,
                uploadProgress = 0f,
            )
        }

        when (
            val result = uploadCv(document) { progress ->
                _state.update {
                    it.copy(
                        uploadProgress = progress.coerceIn(0f, 1f),
                    )
                }
            }
        ) {
            is CareerPilotResult.Error -> {
                _state.update {
                    it.copy(
                        stage = CvUploadStage.SELECTED,
                        uploadProgress = 0f,
                    )
                }
                _effects.emit(
                    UploadCvEffect.ShowError(
                        result.error.toUIText(),
                    )
                )
            }
            is CareerPilotResult.Success -> {
                _state.update {
                    it.copy(
                        stage = CvUploadStage.UPLOADED,
                        uploadProgress = 1f,
                    )
                }
            }
        }
    }

    private fun navigateAfterAnalysis() {
        if (!_state.value.canAnalyze) return

        viewModelScope.launch {
            _effects.emit(UploadCvEffect.NavigateNext)
        }
    }

    private fun skipUpload() {
        uploadJob?.cancel()

        viewModelScope.launch {
            _effects.emit(UploadCvEffect.Skip)
        }
    }
}
