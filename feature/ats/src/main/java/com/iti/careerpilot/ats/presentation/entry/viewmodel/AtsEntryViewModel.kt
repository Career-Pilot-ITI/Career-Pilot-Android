package com.iti.careerpilot.ats.presentation.entry.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iti.careerpilot.ats.R
import com.iti.careerpilot.ats.domain.usecase.ImportJobUseCase
import com.iti.careerpilot.ats.domain.usecase.ObserveCurrentProfileUseCase
import com.iti.careerpilot.ats.domain.usecase.ReplaceCurrentCvUseCase
import com.iti.careerpilot.ats.domain.util.JobUrlParser
import com.iti.careerpilot.ats.presentation.entry.state.AtsEntryAction
import com.iti.careerpilot.ats.presentation.entry.state.AtsEntryEffect
import com.iti.careerpilot.ats.presentation.entry.state.AtsEntryUiState
import com.iti.common.media.pdfpicker.PdfOperations
import com.iti.common.result.CareerPilotResult
import com.iti.common.util.UIText
import com.iti.common.util.toUIText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AtsEntryViewModel @Inject constructor(
    private val observeCurrentProfile: ObserveCurrentProfileUseCase,
    private val replaceCurrentCv: ReplaceCurrentCvUseCase,
    private val importJob: ImportJobUseCase,
    private val pdfOperations: PdfOperations,
) : ViewModel() {
    private val _state = MutableStateFlow(AtsEntryUiState())
    val state = _state.asStateFlow()

    private val effectChannel = Channel<AtsEntryEffect>(Channel.Factory.BUFFERED)
    val effects = effectChannel.receiveAsFlow()

    private var activeOperation: Job? = null
    private var profileObservationJob: Job? = null

    private fun observeProfile() {
        if (profileObservationJob != null) return
        profileObservationJob = viewModelScope.launch {
            observeCurrentProfile().collect { profile ->
                _state.update { current ->
                    current.copy(
                        cvFileName = profile.cv.cvFileName,
                        cvSizeBytes = profile.cv.cvSizeBytes,
                        hasSynchronizedCv = profile.cv.cvUrl.isNotBlank(),
                    )
                }
            }
        }
    }

    fun onAction(action: AtsEntryAction) {
        when (action) {
            AtsEntryAction.Initial -> observeProfile()
            is AtsEntryAction.JobUrlChanged -> updateUrl(action.value)
            is AtsEntryAction.SharedTextReceived -> acceptSharedText(action.value)
            AtsEntryAction.SelectCvClicked -> emitEffect(AtsEntryEffect.OpenPdfPicker)
            is AtsEntryAction.PdfSelected -> uploadPdf(action.uri)
            AtsEntryAction.CompareClicked -> importCurrentJob()
        }
    }

    private fun updateUrl(value: String) {
        val limitedValue = value.take(JobUrlParser.MAX_URL_LENGTH)
        _state.update {
            it.copy(
                jobUrl = limitedValue,
                isUrlValid = JobUrlParser.isValidHttpsUrl(limitedValue),
            )
        }
    }

    private fun acceptSharedText(value: String) {
        val url = JobUrlParser.firstValidHttpsUrl(value)
        if (url == null) {
            emitEffect(AtsEntryEffect.ShowMessage(UIText.StringResource(R.string.ats_invalid_shared_link)))
        } else {
            updateUrl(url)
        }
    }

    private fun uploadPdf(uri: String) {
        if (_state.value.isBusy || activeOperation?.isActive == true) return
        activeOperation = viewModelScope.launch {
            _state.update { it.copy(isUploadingCv = true, uploadProgress = 0) }
            when (val readResult = pdfOperations.readPdf(uri)) {
                is CareerPilotResult.Error -> {
                    effectChannel.send(AtsEntryEffect.ShowMessage(readResult.error.toUIText()))
                }
                is CareerPilotResult.Success -> {
                    when (
                        val uploadResult = replaceCurrentCv(readResult.data) { progress ->
                            _state.update { it.copy(uploadProgress = progress.coerceIn(0, 100)) }
                        }
                    ) {
                        is CareerPilotResult.Error -> effectChannel.send(
                            AtsEntryEffect.ShowMessage(uploadResult.error.toUIText()),
                        )
                        is CareerPilotResult.Success -> effectChannel.send(
                            AtsEntryEffect.ShowMessage(UIText.StringResource(R.string.ats_cv_updated)),
                        )
                    }
                }
            }
            _state.update { it.copy(isUploadingCv = false) }
        }
    }

    private fun importCurrentJob() {
        val current = _state.value
        if (!current.canCompare || activeOperation?.isActive == true) return
        activeOperation = viewModelScope.launch {
            _state.update { it.copy(isImporting = true) }
            when (val result = importJob(current.jobUrl)) {
                is CareerPilotResult.Error -> effectChannel.send(
                    AtsEntryEffect.ShowMessage(result.error.toUIText()),
                )
                is CareerPilotResult.Success -> effectChannel.send(
                    AtsEntryEffect.NavigateToJobDetails(result.data.id),
                )
            }
            _state.update { it.copy(isImporting = false) }
        }
    }

    private fun emitEffect(effect: AtsEntryEffect) {
        viewModelScope.launch { effectChannel.send(effect) }
    }
}
