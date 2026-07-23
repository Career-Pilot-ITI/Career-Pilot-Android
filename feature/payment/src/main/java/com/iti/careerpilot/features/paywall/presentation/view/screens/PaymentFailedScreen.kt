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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
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
import com.iti.careerpilot.features.paywall.domain.model.PaymentFailureReason
import com.iti.careerpilot.features.paywall.presentation.view.components.FailedAnimation
import com.iti.careerpilot.features.paywall.presentation.view.components.PaymentErrorCard
import com.iti.careerpilot.features.paywall.presentation.view.components.ScreenTitle
import com.iti.careerpilot.features.paywall.presentation.viewmodel.PaywallIntent
import com.iti.careerpilot.features.paywall.presentation.viewmodel.PaywallState
import com.iti.careerpilot.features.paywall.presentation.viewmodel.PaywallViewModel
import com.iti.careerpilot.payment.R

@Composable
fun PaymentFailedScreen(
    modifier: Modifier = Modifier
) {
    val viewModel: PaywallViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    PaymentFailedContent(
        state = state,
        onIntent = viewModel::onIntent,
        modifier = modifier
    )
}

@Composable
fun PaymentFailedContent(
    state: PaywallState,
    onIntent: (PaywallIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    val errorIcon = Icons.Default.Close
    val title = when (state.failureReason) {
        PaymentFailureReason.DECLINED -> stringResource(R.string.paywall_failed_title_declined)
        PaymentFailureReason.VERIFICATION_TIMEOUT -> stringResource(R.string.paywall_failed_title_timeout)
    }
    val subtitle = when (state.failureReason) {
        PaymentFailureReason.DECLINED -> stringResource(R.string.paywall_failed_subtitle_declined)
        PaymentFailureReason.VERIFICATION_TIMEOUT -> stringResource(R.string.paywall_failed_subtitle_timeout)
    }
    val errorMessage = when (state.failureReason) {
        PaymentFailureReason.DECLINED -> stringResource(R.string.paywall_error_declined)
        PaymentFailureReason.VERIFICATION_TIMEOUT -> stringResource(R.string.paywall_error_verification_timeout)
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

        FailedAnimation()

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

        PaymentErrorCard(
            errorMessage = errorMessage,
            icon = errorIcon
        )

        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.height(Dimens.SpaceXXL))

        CareerPilotButton(
            text = stringResource(R.string.paywall_try_again),
            onClick = { onIntent(PaywallIntent.TryAgainClicked) },
            variant = ButtonVariant.PRIMARY,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(name = "Light Mode - Declined", showBackground = true)
@Preview(name = "Dark Mode - Declined", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PaymentFailedDeclinedPreview() {
    CareerPilotTheme {
        PaymentFailedContent(
            state = PaywallState(
                failureReason = PaymentFailureReason.DECLINED
            ),
            onIntent = {}
        )
    }
}
