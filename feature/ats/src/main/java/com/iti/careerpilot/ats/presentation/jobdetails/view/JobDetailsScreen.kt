package com.iti.careerpilot.ats.presentation.jobdetails.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iti.careerpilot.ats.R
import com.iti.careerpilot.ats.presentation.components.AtsCenteredTopBar
import com.iti.careerpilot.ats.presentation.components.AtsWorkspaceErrorContent
import com.iti.careerpilot.ats.presentation.components.AtsWorkspaceLoadingContent
import com.iti.careerpilot.ats.presentation.components.JobHeaderCard
import com.iti.careerpilot.ats.presentation.jobdetails.state.JobDetailsEffect
import com.iti.careerpilot.ats.presentation.jobdetails.state.JobDetailsIntent
import com.iti.careerpilot.ats.presentation.jobdetails.state.JobDetailsUiState
import com.iti.careerpilot.ats.presentation.jobdetails.view.components.DescriptionCard
import com.iti.careerpilot.ats.presentation.jobdetails.view.components.JobOverviewCard
import com.iti.careerpilot.ats.presentation.jobdetails.view.components.RequirementsCard
import com.iti.careerpilot.ats.presentation.jobdetails.viewmodel.JobDetailsViewModel
import com.iti.careerpilot.ats.presentation.util.UiStateProvider
import com.iti.careerpilot.ats.presentation.util.rememberUiStateProvider
import com.iti.careerpilot.ats.presentation.util.rememberUiStateValue
import com.iti.careerpilot.core.designsystem.components.CareerPilotButton
import com.iti.careerpilot.core.designsystem.components.CareerPilotCard
import com.iti.careerpilot.core.designsystem.components.CoinTopUpBottomSheet
import com.iti.careerpilot.core.designsystem.components.FeatureGateBottomSheet
import androidx.compose.material3.ExperimentalMaterial3Api
import com.iti.careerpilot.core.access.FeaturePricingMap
import com.iti.careerpilot.core.designsystem.components.FeaturePricingBadge
import com.iti.core.model.FeatureKey

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobDetailsRoot(
    workspaceId: Long,
    onBack: () -> Unit,
    openScore: (Long) -> Unit,
    openJob: (String) -> Unit,
    openPaywall: (showGetCoins: Boolean) -> Unit = {},
    viewModel: JobDetailsViewModel = hiltViewModel(),
) {
    val state = viewModel.state.collectAsStateWithLifecycle()
    val stateProvider = rememberUiStateProvider(state)

    LaunchedEffect(workspaceId) {
        viewModel.onIntent(JobDetailsIntent.Initial(workspaceId))
    }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is JobDetailsEffect.OpenScore -> openScore(effect.workspaceId)
                is JobDetailsEffect.NavigateToPaywall -> openPaywall(effect.showGetCoins)
            }
        }
    }

    JobDetailsScreen(
        stateProvider = stateProvider,
        onIntent = viewModel::onIntent,
        onBack = onBack,
        onOpenJob = openJob,
        modifier = Modifier.windowInsetsPadding(WindowInsets.systemBars),
    )

    val showGateSheet by rememberUiStateValue(stateProvider) { it.showGateSheet }
    val gateRequiredPlan by rememberUiStateValue(stateProvider) { it.gateRequiredPlan }
    val gatePlanFeatures by rememberUiStateValue(stateProvider) { it.gatePlanFeatures }
    val gateFeatureName by rememberUiStateValue(stateProvider) { it.gateFeatureName }
    val showCoinTopUpSheet by rememberUiStateValue(stateProvider) { it.showCoinTopUpSheet }
    val coinTopUpRequiredCost by rememberUiStateValue(stateProvider) { it.coinTopUpRequiredCost }
    val coinBalance by rememberUiStateValue(stateProvider) { it.coinBalance }

    if (showGateSheet) {
        FeatureGateBottomSheet(
            featureName = gateFeatureName.ifEmpty { stringResource(R.string.ats_job_match_title) },
            requiredPlan = gateRequiredPlan,
            planFeatures = gatePlanFeatures,
            onUpgradeClick = { viewModel.onIntent(JobDetailsIntent.UpgradeFromGate) },
            onDismiss = { viewModel.onIntent(JobDetailsIntent.DismissGateSheet) },
        )
    }

    if (showCoinTopUpSheet) {
        CoinTopUpBottomSheet(
            coinCost = coinTopUpRequiredCost,
            currentBalance = coinBalance,
            onBuyCoins = { viewModel.onIntent(JobDetailsIntent.BuyCoinsClicked) },
            onDismiss = { viewModel.onIntent(JobDetailsIntent.DismissCoinTopUpSheet) },
        )
    }
}

@Composable
fun JobDetailsScreen(
    stateProvider: UiStateProvider<JobDetailsUiState>,
    onIntent: (JobDetailsIntent) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    onOpenJob: (String) -> Unit = {},
) {
    Column(modifier = modifier.fillMaxSize()) {
        AtsCenteredTopBar(
            title = stringResource(R.string.ats_job_description_title),
            onBack = onBack,
        )

        JobDetailsBody(
            stateProvider = stateProvider,
            onIntent = onIntent,
            onOpenJob = onOpenJob,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
private fun JobDetailsBody(
    stateProvider: UiStateProvider<JobDetailsUiState>,
    onIntent: (JobDetailsIntent) -> Unit,
    onOpenJob: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val phase by rememberUiStateValue(stateProvider) { state ->
        when {
            state.isLoading -> JobDetailsPhase.LOADING
            state.workspace == null -> JobDetailsPhase.ERROR
            else -> JobDetailsPhase.CONTENT
        }
    }

    when (phase) {
        JobDetailsPhase.LOADING -> AtsWorkspaceLoadingContent(modifier = modifier)
        JobDetailsPhase.ERROR -> JobDetailsErrorContent(
            stateProvider = stateProvider,
            onIntent = onIntent,
            modifier = modifier,
        )
        JobDetailsPhase.CONTENT -> JobDetailsContent(
            stateProvider = stateProvider,
            onIntent = onIntent,
            onOpenJob = onOpenJob,
            modifier = modifier,
        )
    }
}

@Composable
private fun JobDetailsErrorContent(
    stateProvider: UiStateProvider<JobDetailsUiState>,
    onIntent: (JobDetailsIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val error by rememberUiStateValue(stateProvider) { it.error }
    AtsWorkspaceErrorContent(
        error = error,
        onRetry = { onIntent(JobDetailsIntent.Retry) },
        modifier = modifier,
    )
}

@Composable
private fun JobDetailsContent(
    stateProvider: UiStateProvider<JobDetailsUiState>,
    onIntent: (JobDetailsIntent) -> Unit,
    onOpenJob: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val workspace by rememberUiStateValue(stateProvider) { it.workspace }
    val currentWorkspace = workspace
    if (currentWorkspace == null) {
        val error by rememberUiStateValue(stateProvider) { it.error }
        AtsWorkspaceErrorContent(
            error = error,
            onRetry = { onIntent(JobDetailsIntent.Retry) },
            modifier = modifier,
        )
        return
    }

    val atsScoreAccess by rememberUiStateValue(stateProvider) { it.atsScoreAccess }
    val coinBalance by rememberUiStateValue(stateProvider) { it.coinBalance }
    val planDisplayName by rememberUiStateValue(stateProvider) { it.planDisplayName }

    Column(
        modifier = modifier
            .padding(horizontal = 20.dp)
            .padding(top = 8.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item { Spacer(Modifier.height(2.dp)) }
            item { JobHeaderCard(job = currentWorkspace.job, onOpenJob = onOpenJob) }
            item { JobOverviewCard(job = currentWorkspace.job) }

            if (currentWorkspace.job.description.isNotBlank()) {
                item { DescriptionCard(currentWorkspace.job.description) }
            }
            if (
                currentWorkspace.job.requiredSkills.isNotEmpty() ||
                currentWorkspace.job.preferredSkills.isNotEmpty() ||
                currentWorkspace.job.technologies.isNotEmpty()
            ) {
                item { RequirementsCard(currentWorkspace.job) }
            }

            item {
                AtsFeaturesPricingCard()
            }

            item { Spacer(Modifier.height(8.dp)) }
        }

        FeaturePricingBadge(
            access = atsScoreAccess,
            coinBalance = coinBalance,
            planDisplayName = planDisplayName,
            onUpgradeClick = { onIntent(JobDetailsIntent.UpgradeFromGate) },
        )

        CareerPilotButton(
            text = stringResource(R.string.ats_start_scoring),
            onClick = { onIntent(JobDetailsIntent.StartScoring) },
        )
    }
}

@Composable
private fun AtsFeaturesPricingCard(modifier: Modifier = Modifier) {
    CareerPilotCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.ats_job_match),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.ats_score_cv),
                    style = MaterialTheme.typography.bodyMedium,
                )
                FeaturePricingBadge(
                    coinCost = FeaturePricingMap.coinCost(FeatureKey.AtsFeatures),
                    compact = true,
                )
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.ats_optimize_cv),
                    style = MaterialTheme.typography.bodyMedium,
                )
                FeaturePricingBadge(
                    coinCost = FeaturePricingMap.coinCost(FeatureKey.CvAiAnalysis),
                    compact = true,
                )
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.cover_letter),
                    style = MaterialTheme.typography.bodyMedium,
                )
                FeaturePricingBadge(
                    coinCost = FeaturePricingMap.coinCost(FeatureKey.CoverLetter),
                    compact = true,
                )
            }
        }
    }
}

private enum class JobDetailsPhase { LOADING, ERROR, CONTENT }
