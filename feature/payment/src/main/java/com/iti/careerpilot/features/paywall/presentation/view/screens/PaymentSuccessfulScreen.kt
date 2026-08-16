package com.iti.careerpilot.features.paywall.presentation.view.screens

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iti.careerpilot.core.designsystem.CareerPilotTheme
import com.iti.careerpilot.core.designsystem.CareerPilotTypography
import com.iti.careerpilot.core.designsystem.Dimens
import com.iti.careerpilot.core.designsystem.components.ButtonVariant
import com.iti.careerpilot.core.designsystem.components.CareerPilotButton
import com.iti.careerpilot.features.paywall.presentation.view.components.CoinPurchaseSuccessCard
import com.iti.careerpilot.features.paywall.presentation.view.components.ScreenTitle
import com.iti.careerpilot.features.paywall.presentation.view.components.SuccessAnimation
import com.iti.careerpilot.features.paywall.presentation.view.components.SuccessBenefitsCard
import com.iti.careerpilot.features.paywall.presentation.viewmodel.CheckoutItemType
import com.iti.careerpilot.features.paywall.presentation.viewmodel.PaywallIntent
import com.iti.careerpilot.features.paywall.presentation.viewmodel.PaywallState
import com.iti.careerpilot.features.paywall.presentation.viewmodel.PaywallViewModel
import com.iti.careerpilot.payment.R

@Composable
fun PaymentSuccessfulScreen(
    modifier: Modifier = Modifier
) {
    val viewModel: PaywallViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    PaymentSuccessfulContent(
        state = state,
        onIntent = viewModel::onIntent,
        modifier = modifier
    )
}

@Composable
fun PaymentSuccessfulContent(
    state: PaywallState,
    onIntent: (PaywallIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    val title = when (state.checkoutItemType) {
        CheckoutItemType.COIN_PACK -> stringResource(R.string.paywall_coins_successful_title)
        CheckoutItemType.SUBSCRIPTION -> stringResource(R.string.paywall_successful_title)
    }

    val subtitle = when (state.checkoutItemType) {
        CheckoutItemType.COIN_PACK -> {
            stringResource(
                R.string.paywall_coins_successful_subtitle,
                state.successfulCoinCount,
                state.coinBalance
            )
        }
        CheckoutItemType.SUBSCRIPTION -> {
            val planName = stringResource(id = state.successfulPlanNameRes)
            stringResource(R.string.paywall_successful_subtitle_plan, planName)
        }
    }

    val benefits = when (state.checkoutItemType) {
        CheckoutItemType.COIN_PACK -> listOf(
            stringResource(R.string.paywall_coins_benefit_added, state.successfulCoinCount),
            stringResource(R.string.paywall_coins_benefit_unlock),
            stringResource(R.string.paywall_coins_benefit_no_expiry)
        )
        CheckoutItemType.SUBSCRIPTION -> {
            (state.selectedPlan?.features ?: state.unlockedFeatures).map { stringResource(id = it) }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding()
            .padding(Dimens.SpaceL),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))

        SuccessAnimation()

        Spacer(modifier = Modifier.height(Dimens.SpaceXXL))

        ScreenTitle(
            title = title,
            modifier = Modifier.padding(bottom = Dimens.SpaceS),
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = subtitle,
            style = CareerPilotTypography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(Dimens.SpaceXXL))

        if (state.checkoutItemType == CheckoutItemType.COIN_PACK) {
            CoinPurchaseSuccessCard(
                coinCount = state.successfulCoinCount,
                totalBalance = state.coinBalance
            )
        } else {
            SuccessBenefitsCard(benefits = benefits)
        }

        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.height(Dimens.SpaceXXL))

        CareerPilotButton(
            text = stringResource(R.string.paywall_start_practising),
            onClick = { onIntent(PaywallIntent.StartPractisingClicked) },
            variant = ButtonVariant.PRIMARY,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(name = "Light Mode - Plan", showBackground = true)
@Preview(name = "Dark Mode - Coins", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PaymentSuccessfulScreenPreview() {
    CareerPilotTheme {
        PaymentSuccessfulContent(
            state = PaywallState(
                coinBalance = 700,
                purchasedCoinCount = 500,
                checkoutItemType = CheckoutItemType.COIN_PACK
            ),
            onIntent = {}
        )
    }
}
