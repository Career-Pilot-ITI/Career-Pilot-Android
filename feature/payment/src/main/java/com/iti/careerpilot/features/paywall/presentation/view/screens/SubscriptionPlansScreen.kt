package com.iti.careerpilot.features.paywall.presentation.view.screens

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iti.careerpilot.core.designsystem.CareerPilotTheme
import com.iti.careerpilot.core.designsystem.Dimens
import com.iti.careerpilot.core.designsystem.components.ButtonVariant
import com.iti.careerpilot.core.designsystem.components.CareerPilotButton
import com.iti.careerpilot.features.paywall.presentation.view.components.PlanSelectorRow
import com.iti.careerpilot.features.paywall.presentation.view.components.SubscriptionPlanDetailsCard
import com.iti.careerpilot.features.paywall.presentation.viewmodel.PaywallIntent
import com.iti.careerpilot.features.paywall.presentation.viewmodel.PaywallState
import com.iti.careerpilot.features.paywall.presentation.viewmodel.PaywallViewModel
import com.iti.careerpilot.payment.R

@Composable
fun SubscriptionPlansScreen(
    onNavigateToGetCoins: () -> Unit = {},
    onNavigateToMonthlyLimit: () -> Unit = {},
    onNavigateBack: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: PaywallViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.onIntent(PaywallIntent.LoadSubscriptionPlans)
    }

    val state by viewModel.state.collectAsStateWithLifecycle()

    SubscriptionPlansContent(
        state = state,
        onIntent = viewModel::onIntent,
        onNavigateToMonthlyLimit = onNavigateToMonthlyLimit,
        onBack = onNavigateBack,
        modifier = modifier
    )
}

@Composable
fun SubscriptionPlansContent(
    state: PaywallState,
    onIntent: (PaywallIntent) -> Unit,
    onNavigateToMonthlyLimit: () -> Unit = {},
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val selectedPlan = state.selectedPlan
    val selectedPlanName = stringResource(id = selectedPlan?.nameRes ?: R.string.paywall_plan_plus)
    val isButtonEnabled = state.isPlanChangeEnabled

    val buttonText = when {
        state.isUpgrade -> stringResource(R.string.paywall_upgrade_to_plan, selectedPlanName)
        state.isDowngrade -> stringResource(R.string.paywall_downgrade_to_plan, selectedPlanName)
        else -> stringResource(R.string.paywall_current_plan_button)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.paywall_choose_plan_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.paywall_back),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(start = Dimens.SpaceL, end = Dimens.SpaceL, top = Dimens.SpaceM, bottom = Dimens.SpaceL)
            ) {
                CareerPilotButton(
                    text = buttonText,
                    onClick = { if (isButtonEnabled) onIntent(PaywallIntent.ConfirmUpgradeRequested) },
                    enabled = isButtonEnabled,
                    variant = if (isButtonEnabled) ButtonVariant.PRIMARY else ButtonVariant.OUTLINE,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Dimens.OtpCellHeight)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = Dimens.SpaceL)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(Dimens.SpaceM))

            Text(
                text = stringResource(R.string.paywall_choose_plan_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(Dimens.SpaceL))

            PlanSelectorRow(
                plans = state.subscriptionPlans,
                selectedPlanId = state.selectedPlanId,
                currentTierId = state.currentTierId,
                onSelectPlan = { onIntent(PaywallIntent.SelectPlan(it)) }
            )

            Spacer(modifier = Modifier.height(Dimens.SpaceL))

            SubscriptionPlanDetailsCard(
                plan = selectedPlan,
                selectedPlanName = selectedPlanName,
                isCurrentPlan = state.selectedLevel == state.currentTierLevel,
                isLoadingTierPrices = state.isLoadingTierPrices
            )

            Spacer(modifier = Modifier.height(Dimens.SpaceL))
        }
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SubscriptionPlansScreenPreview() {
    CareerPilotTheme {
        SubscriptionPlansContent(
            state = PaywallState(),
            onIntent = {}
        )
    }
}
