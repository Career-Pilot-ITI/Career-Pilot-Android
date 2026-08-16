package com.iti.careerpilot.ats.presentation.entry.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.iti.careerpilot.ats.R
import com.iti.careerpilot.ats.presentation.entry.state.AtsEntryEffect
import com.iti.careerpilot.ats.presentation.entry.state.AtsEntryIntent
import com.iti.careerpilot.ats.presentation.entry.state.AtsEntryUiState
import com.iti.careerpilot.ats.presentation.entry.view.components.AtsScreenHeader
import com.iti.careerpilot.ats.presentation.entry.view.components.FasterShareHintCard
import com.iti.careerpilot.ats.presentation.entry.view.components.JobUrlTextField
import com.iti.careerpilot.ats.presentation.entry.viewmodel.AtsEntryViewModel
import com.iti.careerpilot.ats.presentation.util.UiStateProvider
import com.iti.careerpilot.ats.presentation.util.rememberUiStateProvider
import com.iti.careerpilot.ats.presentation.util.rememberUiStateValue
import com.iti.careerpilot.core.designsystem.components.CareerPilotButton
import com.iti.careerpilot.core.designsystem.components.CareerPilotCard
import com.iti.careerpilot.core.designsystem.components.CvUploadCard
import com.iti.careerpilot.core.designsystem.components.CvUploadCardStage
import com.iti.careerpilot.core.designsystem.components.FeatureGateBottomSheet
import com.iti.common.snackbar.CareerPilotSnackbarController
import com.iti.core.model.Plan
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AtsEntryRoot(
    initialSharedText: String?,
    onSharedTextConsumed: () -> Unit,
    onJobDetailsRequested: (Long) -> Unit,
    onEditProfileRequested: () -> Unit,
    onPaywallRequested: () -> Unit = {},
    viewModel: AtsEntryViewModel = hiltViewModel(),
) {
    val state = viewModel.state.collectAsStateWithLifecycle()
    val stateProvider = rememberUiStateProvider(state)

    LaunchedEffect(viewModel) {
        viewModel.onIntent(AtsEntryIntent.Initial)
    }

    LaunchedEffect(initialSharedText) {
        initialSharedText?.let {
            viewModel.onIntent(AtsEntryIntent.SharedTextReceived(it))
            onSharedTextConsumed()
        }
    }

    val lifecycle = LocalLifecycleOwner.current
    LaunchedEffect(lifecycle) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.effects.collect { effect ->
                when (effect) {
                    AtsEntryEffect.NavigateToEditProfile -> onEditProfileRequested()
                    is AtsEntryEffect.NavigateToJobDetails -> onJobDetailsRequested(effect.workspaceId)
                    is AtsEntryEffect.ShowMessage -> CareerPilotSnackbarController.show(effect.message)
                    AtsEntryEffect.NavigateToPaywall -> onPaywallRequested()
                }
            }
        }
    }

    AtsEntryScreen(
        stateProvider = stateProvider,
        onIntent = viewModel::onIntent,
        modifier = Modifier.windowInsetsPadding(WindowInsets.systemBars),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AtsEntryScreen(
    stateProvider: UiStateProvider<AtsEntryUiState>,
    onIntent: (AtsEntryIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val jobUrlDescription = stringResource(R.string.ats_job_posting_link)
    val importMessages = persistentListOf(
        stringResource(R.string.ats_importing_job),
        stringResource(R.string.ats_importing_details),
        stringResource(R.string.ats_preparing_workspace),
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .padding(bottom = 12.dp)
            .imePadding(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(top = 24.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            item {
                AtsScreenHeader()
                Text(
                    text = stringResource(R.string.ats_compare_title),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 10.dp),
                )
                Text(
                    text = stringResource(R.string.ats_compare_subtitle),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }

            item { Spacer(Modifier.height(12.dp)) }

            item {
                Text(
                    text = stringResource(R.string.ats_job_posting_link),
                    style = MaterialTheme.typography.labelLarge,
                )

                JobUrlTextField(
                    stateProvider = stateProvider,
                    onIntent = onIntent,
                    jobUrlDescription = jobUrlDescription,
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = stringResource(R.string.ats_supported_job_sites),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }

            item {
                Text(
                    text = stringResource(R.string.ats_cv_on_file),
                    style = MaterialTheme.typography.labelLarge,
                )

                Spacer(Modifier.height(8.dp))

                CvUploadSection(
                    stateProvider = stateProvider,
                    onIntent = onIntent,
                )
            }

            item {
                FasterShareHintCard()
            }
        }

        CompareButton(
            stateProvider = stateProvider,
            importMessages = importMessages,
            onIntent = onIntent,
        )
    }

    val showGateSheet by rememberUiStateValue(stateProvider) { it.showGateSheet }
    val gateRequiredPlan by rememberUiStateValue(stateProvider) { it.gateRequiredPlan }
    val gatePlanFeatures by rememberUiStateValue(stateProvider) { it.gatePlanFeatures }
    val gateFeatureName by rememberUiStateValue(stateProvider) { it.gateFeatureName }

    if (showGateSheet) {
        FeatureGateBottomSheet(
            featureName = gateFeatureName.ifEmpty { stringResource(R.string.ats_job_match_title) },
            requiredPlan = gateRequiredPlan ?: Plan.PLUS,
            planFeatures = gatePlanFeatures,
            onUpgradeClick = { onIntent(AtsEntryIntent.UpgradeFromGate) },
            onDismiss = { onIntent(AtsEntryIntent.DismissGateSheet) },
        )
    }
}

@Composable
private fun CvUploadSection(
    stateProvider: UiStateProvider<AtsEntryUiState>,
    onIntent: (AtsEntryIntent) -> Unit,
) {
    val fileName by rememberUiStateValue(stateProvider) { it.cvFileName }
    val fileSizeBytes by rememberUiStateValue(stateProvider) { it.cvSizeBytes }
    val hasSynchronizedCv by rememberUiStateValue(stateProvider) { it.hasSynchronizedCv }

    if (hasSynchronizedCv) {
        CvUploadCard(
            fileName = fileName.takeIf(String::isNotBlank)
                ?: stringResource(R.string.ats_current_cv),
            fileSizeBytes = fileSizeBytes,
            stage = CvUploadCardStage.UPLOADED,
            uploadProgress = 100,
            onClick = {},
            enabled = false,
        )
    } else {
        CareerPilotCard(
            useShadow = false,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = stringResource(R.string.ats_missing_cv_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = stringResource(R.string.ats_missing_cv_message),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                CareerPilotButton(
                    text = stringResource(R.string.ats_open_edit_profile),
                    onClick = { onIntent(AtsEntryIntent.EditProfileClicked) },
                )
            }
        }
    }
}

@Composable
private fun CompareButton(
    stateProvider: UiStateProvider<AtsEntryUiState>,
    importMessages: ImmutableList<String>,
    onIntent: (AtsEntryIntent) -> Unit,
) {
    val isImporting by rememberUiStateValue(stateProvider) { it.isImporting }
    val canCompare by rememberUiStateValue(stateProvider) { it.canCompare }
    var importMessageIndex by rememberSaveable { mutableIntStateOf(0) }

    LaunchedEffect(isImporting) {
        if (!isImporting) {
            importMessageIndex = 0
            return@LaunchedEffect
        }
        while (true) {
            delay(1_600)
            importMessageIndex = (importMessageIndex + 1) % importMessages.size
        }
    }

    CareerPilotButton(
        text = if (isImporting) {
            importMessages[importMessageIndex]
        } else {
            stringResource(R.string.ats_compare_now)
        },
        onClick = { onIntent(AtsEntryIntent.CompareClicked) },
        enabled = canCompare,
    )
}
