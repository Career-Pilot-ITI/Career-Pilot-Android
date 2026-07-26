package com.iti.careerpilot.features.paywall.presentation.view.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.iti.careerpilot.features.paywall.presentation.view.components.MonthlyLimitHeader
import com.iti.careerpilot.features.paywall.presentation.view.components.MonthlyLimitProgressCard
import com.iti.careerpilot.features.paywall.presentation.view.components.ScreenTitle
import com.iti.careerpilot.features.paywall.presentation.viewmodel.PaywallIntent
import com.iti.careerpilot.features.paywall.presentation.viewmodel.PaywallState
import com.iti.careerpilot.features.paywall.presentation.viewmodel.PaywallViewModel
import com.iti.careerpilot.payment.R

@Composable
fun MonthlyLimitScreen(
    viewModel: PaywallViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    MonthlyLimitContent(
        state = state,
        onIntent = viewModel::onIntent
    )
}

@Composable
fun MonthlyLimitContent(
    state: PaywallState,
    onIntent: (PaywallIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding()
            .padding(Dimens.SpaceL),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        MonthlyLimitHeader()

        Spacer(modifier = Modifier.height(Dimens.SpaceL))

        ScreenTitle(
            title = stringResource(id = R.string.paywall_monthly_limit_reached),
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(Dimens.SpaceS))

        Text(
            text = stringResource(id = R.string.paywall_monthly_limit_subtitle),
            style = CareerPilotTypography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(Dimens.SpaceXXXL))

        MonthlyLimitProgressCard(
            usedSessions = state.usedFreeSessions,
            maxSessions = state.maxFreeSessions,
            resetDate = state.resetDate
        )

        Spacer(modifier = Modifier.height(Dimens.SpaceXXXXL))

        CareerPilotButton(
            text = stringResource(id = R.string.paywall_upgrade_now),
            onClick = { onIntent(PaywallIntent.UpgradeNowRequested) },
            variant = ButtonVariant.PRIMARY,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(Dimens.SpaceM))

        TextButton(
            onClick = { onIntent(PaywallIntent.WaitUntilNextMonthRequested) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = stringResource(id = R.string.paywall_wait_next_month),
                style = CareerPilotTypography.labelLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MonthlyLimitScreenPreview() {
    CareerPilotTheme {
        MonthlyLimitContent(
            state = PaywallState(),
            onIntent = {}
        )
    }
}
