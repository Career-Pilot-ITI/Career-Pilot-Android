package com.iti.careerpilot.ats.presentation.entry.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iti.careerpilot.ats.R
import com.iti.careerpilot.ats.domain.usecase.ImportJobUseCase
import com.iti.careerpilot.ats.domain.usecase.ObserveCurrentProfileUseCase
import com.iti.careerpilot.ats.domain.util.JobUrlParser
import com.iti.careerpilot.ats.presentation.entry.state.AtsEntryEffect
import com.iti.careerpilot.ats.presentation.entry.state.AtsEntryIntent
import com.iti.careerpilot.ats.presentation.entry.state.AtsEntryUiState
import com.iti.careerpilot.core.access.PlanAccessMap
import com.iti.careerpilot.core.access.domain.usecase.CheckFeatureAccessUseCase
import com.iti.careerpilot.core.access.domain.usecase.RefreshAccessUseCase
import com.iti.common.media.pdfpicker.PdfOperations
import com.iti.common.result.CareerPilotResult
import com.iti.common.util.UIText
import com.iti.common.util.toUIText
import com.iti.core.model.FeatureAccess
import com.iti.core.model.FeatureKey
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AtsEntryViewModel @Inject constructor(
    private val observeCurrentProfile: ObserveCurrentProfileUseCase,
    private val importJob: ImportJobUseCase,
    private val pdfOperations: PdfOperations,
    private val checkFeatureAccess: CheckFeatureAccessUseCase,
    private val refreshAccess: RefreshAccessUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(AtsEntryUiState())
    val state = _state.asStateFlow()

    private val effectChannel = Channel<AtsEntryEffect>(Channel.BUFFERED)
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

    fun onIntent(intent: AtsEntryIntent) {
        when (intent) {
            AtsEntryIntent.Initial -> observeProfile()
            is AtsEntryIntent.JobUrlChanged -> updateUrl(intent.value)
            is AtsEntryIntent.SharedTextReceived -> acceptSharedText(intent.value)
            AtsEntryIntent.SelectCvClicked -> emitEffect(AtsEntryEffect.OpenPdfPicker)
            is AtsEntryIntent.PdfSelected -> uploadPdf(intent.uri)
            AtsEntryIntent.CompareClicked -> importCurrentJob()
            AtsEntryIntent.DismissGateSheet -> _state.update { it.copy(showGateSheet = false) }
            AtsEntryIntent.DismissCoinTopUpSheet -> _state.update { it.copy(showCoinTopUpSheet = false) }
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

    private fun importCurrentJob() {
        val current = _state.value
        if (!current.canCompare || activeOperation?.isActive == true) return
        activeOperation = viewModelScope.launch {
            val access = checkFeatureAccess(FeatureKey.JobParse).first()
            when (access) {
                is FeatureAccess.Locked -> {
                    _state.update {
                        it.copy(
                            showGateSheet = true,
                            gatePlanFeatures = PlanAccessMap.featuresFor(access.requiredPlan).map { f -> f.displayName() },
                            gateRequiredPlan = access.requiredPlan,
                            gateFeatureName = FeatureKey.JobParse.displayName(),
                        )
                    }
                    return@launch
                }
                is FeatureAccess.CoinTopUpRequired -> {
                    _state.update {
                        it.copy(
                            showCoinTopUpSheet = true,
                            coinTopUpRequiredCost = access.coinCost,
                            hasInsufficientCoins = true,
                        )
                    }
                    return@launch
                }
                is FeatureAccess.StaleCacheBlocked -> {
                    refreshAccess()
                    return@launch
                }
                else -> Unit
            }

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
