package com.iti.careerpilot.features.paywall.presentation.view.screens

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iti.careerpilot.core.designsystem.CareerPilotTheme
import com.iti.careerpilot.core.designsystem.Dimens
import com.iti.careerpilot.core.designsystem.components.ButtonVariant
import com.iti.careerpilot.core.designsystem.components.CareerPilotButton
import com.iti.careerpilot.features.paywall.presentation.view.components.PlanFeaturesList
import com.iti.careerpilot.features.paywall.presentation.viewmodel.PaywallIntent
import com.iti.careerpilot.features.paywall.presentation.viewmodel.PaywallState
import com.iti.careerpilot.features.paywall.presentation.viewmodel.PaywallViewModel
import com.iti.careerpilot.payment.R

@Composable
fun PaywallRootScreen(
    showGetCoins: Boolean = false,
    onNavigateBack: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: PaywallViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.onIntent(PaywallIntent.LoadSubscriptionPlans)
    }

    val state by viewModel.state.collectAsStateWithLifecycle()

    PaywallRootContent(
        state = state,
        onIntent = viewModel::onIntent,
        onNavigateToChoosePlan = { viewModel.onIntent(PaywallIntent.UpgradeNowRequested) },
        onNavigateToGetCoins = { viewModel.onIntent(PaywallIntent.TestCoinsClicked) },
        onBack = onNavigateBack,
        modifier = modifier
    )
}

@Composable
fun PaywallRootContent(
    state: PaywallState,
    onIntent: (PaywallIntent) -> Unit,
    onNavigateToChoosePlan: () -> Unit = {},
    onNavigateToGetCoins: () -> Unit = {},
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.SpaceS, vertical = Dimens.SpaceS)
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.paywall_back),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(start = Dimens.SpaceL, end = Dimens.SpaceL, top = Dimens.SpaceM, bottom = Dimens.SpaceL)
            ) {
                CareerPilotButton(
                    text = stringResource(R.string.paywall_upgrade_now),
                    onClick = { onNavigateToChoosePlan() },
                    variant = ButtonVariant.PRIMARY,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(Dimens.SpaceS))
                CareerPilotButton(
                    text = stringResource(R.string.paywall_test_coins),
                    onClick = { onNavigateToGetCoins() },
                    variant = ButtonVariant.OUTLINE,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = Dimens.SpaceL)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(Dimens.SpaceM))

            Box(
                modifier = Modifier
                    .size(Dimens.LockIconBoxSize)
                    .background(
                        Brush.linearGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.secondary
                            )
                        ),
                        RoundedCornerShape(Dimens.SpaceL)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.WorkspacePremium,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(Dimens.SpaceXXXXL)
                )
            }

            Spacer(modifier = Modifier.height(Dimens.SpaceL))

            Text(
                text = stringResource(R.string.paywall_choose_plan_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(Dimens.SpaceS))

            Text(
                text = stringResource(R.string.paywall_choose_plan_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(Dimens.SpaceXXL))

            PlanFeaturesList(features = state.unlockedFeatures)

            Spacer(modifier = Modifier.height(Dimens.SpaceL))
        }
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PaywallRootScreenPreview() {
    CareerPilotTheme {
        PaywallRootContent(
            state = PaywallState(),
            onIntent = {}
        )
    }
}
