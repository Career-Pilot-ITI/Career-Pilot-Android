package com.iti.careerpilot.features.paywall.presentation.view.screens

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.iti.careerpilot.core.designsystem.CareerPilotPalette
import com.iti.careerpilot.core.designsystem.CareerPilotTheme
import com.iti.careerpilot.core.designsystem.CareerPilotTypography
import com.iti.careerpilot.core.designsystem.Dimens
import com.iti.careerpilot.core.designsystem.components.ButtonVariant
import com.iti.careerpilot.core.designsystem.components.CareerPilotButton
import com.iti.careerpilot.core.designsystem.components.CareerPilotCard
import com.iti.careerpilot.features.paywall.presentation.viewmodel.MySubscriptionEffect
import com.iti.careerpilot.features.paywall.presentation.viewmodel.MySubscriptionIntent
import com.iti.careerpilot.features.paywall.presentation.viewmodel.MySubscriptionUiState
import com.iti.careerpilot.features.paywall.presentation.viewmodel.MySubscriptionViewModel
import com.iti.common.snackbar.CareerPilotSnackbarController
import com.iti.common.util.UIText
import com.iti.core.model.SubscriptionInfo
import com.iti.careerpilot.payment.R

@Composable
fun MySubscriptionScreen(
    onNavigateToChoosePlan: () -> Unit,
    onNavigateToGetCoins: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MySubscriptionViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(viewModel.effects, lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.effects.collect { effect ->
                when (effect) {
                    MySubscriptionEffect.NavigateToChoosePlan -> onNavigateToChoosePlan()
                    MySubscriptionEffect.NavigateToGetCoins -> onNavigateToGetCoins()
                    MySubscriptionEffect.NavigateBack -> onNavigateBack()
                    is MySubscriptionEffect.ShowSnackbar -> {
                        CareerPilotSnackbarController.show(UIText.DynamicString(effect.message))
                    }
                }
            }
        }
    }

    MySubscriptionScreenContent(
        state = state,
        onIntent = viewModel::onIntent,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MySubscriptionScreenContent(
    state: MySubscriptionUiState,
    onIntent: (MySubscriptionIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.paywall_my_subscription_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onIntent(MySubscriptionIntent.BackClicked) }) {
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
        }
    ) { innerPadding ->
        when {
            state.isLoading -> {
                Box(
                    modifier = modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }

            state.error != null && state.subscriptionInfo == null -> {
                Box(
                    modifier = modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(Dimens.SpaceL),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ErrorOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(Dimens.ErrorIconSize)
                        )
                        Spacer(modifier = Modifier.height(Dimens.SpaceM))
                        Text(
                            text = state.error,
                            style = CareerPilotTypography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(Dimens.SpaceL))
                        CareerPilotButton(
                            text = stringResource(R.string.paywall_subscription_retry),
                            onClick = { onIntent(MySubscriptionIntent.LoadSubscription) },
                            variant = ButtonVariant.PRIMARY,
                            modifier = Modifier.fillMaxWidth(0.6f)
                        )
                    }
                }
            }

            state.subscriptionInfo != null -> {
                val info = state.subscriptionInfo
                val isFreeTier = info.tier.equals("FREE", ignoreCase = true)
                val canCancel = !isFreeTier && info.isActive && info.cancelledAt == null

                Column(
                    modifier = modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = Dimens.SpaceL)
                        .verticalScroll(rememberScrollState())
                ) {
                    Spacer(modifier = Modifier.height(Dimens.SpaceM))

                    // 1. Subscription Details Card
                    SubscriptionDetailsCard(info = info)

                    Spacer(modifier = Modifier.height(Dimens.SpaceL))

                    // 2. Included in Your Plan Card
                    PlanBenefitsCard(planFeatures = state.planFeatures)

                    Spacer(modifier = Modifier.height(Dimens.SpaceXL))

                    // 3. Action Buttons
                    CareerPilotButton(
                        text = stringResource(R.string.paywall_change_plan_cta),
                        onClick = { onIntent(MySubscriptionIntent.UpgradePlanClicked) },
                        variant = ButtonVariant.PRIMARY,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(Dimens.SpaceM))

                    CareerPilotButton(
                        text = stringResource(R.string.paywall_top_up_coins_cta),
                        onClick = { onIntent(MySubscriptionIntent.TopUpCoinsClicked) },
                        variant = ButtonVariant.SECONDARY,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (canCancel) {
                        Spacer(modifier = Modifier.height(Dimens.SpaceM))

                        CareerPilotButton(
                            text = if (state.isCancelling) "Cancelling..." else stringResource(R.string.paywall_cancel_subscription_cta),
                            onClick = { onIntent(MySubscriptionIntent.ShowCancelDialog) },
                            variant = ButtonVariant.GHOST,
                            enabled = !state.isCancelling,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(Dimens.SpaceXL))
                    Spacer(modifier = Modifier.navigationBarsPadding())
                }
            }
        }
    }

    if (state.showCancelConfirmDialog) {
        AlertDialog(
            onDismissRequest = { onIntent(MySubscriptionIntent.DismissCancelDialog) },
            title = {
                Text(
                    text = stringResource(R.string.paywall_cancel_dialog_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.paywall_cancel_dialog_message),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { onIntent(MySubscriptionIntent.ConfirmCancelSubscription) }
                ) {
                    Text(
                        text = stringResource(R.string.paywall_cancel_dialog_confirm),
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { onIntent(MySubscriptionIntent.DismissCancelDialog) }
                ) {
                    Text(
                        text = stringResource(R.string.paywall_cancel_dialog_dismiss),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        )
    }
}

@Composable
private fun SubscriptionDetailsCard(
    info: SubscriptionInfo,
    modifier: Modifier = Modifier
) {
    val tierDisplayName = info.tier.lowercase().replaceFirstChar { it.uppercase() }
    val planIcon = when (info.tier.uppercase().trim()) {
        "PLUS" -> Icons.Rounded.Star
        "PRO", "MAX" -> Icons.Rounded.WorkspacePremium
        else -> Icons.Rounded.Person
    }

    CareerPilotCard(
        elevation = Dimens.SpaceS,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.SpaceL)
        ) {
            // Header: Tier Icon + Plan Title + Status Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(Dimens.ButtonHeight)
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.secondary
                                    )
                                ),
                                RoundedCornerShape(Dimens.SpaceM)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = planIcon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(Dimens.SpaceXXL)
                        )
                    }

                    Spacer(modifier = Modifier.width(Dimens.SpaceM))

                    Column {
                        Text(
                            text = stringResource(R.string.paywall_plan_name_suffix, tierDisplayName),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = stringResource(R.string.paywall_my_subscription_current_plan),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Status Pill
                StatusPill(
                    isActive = info.isActive && info.cancelledAt == null
                )
            }

            Spacer(modifier = Modifier.height(Dimens.SpaceM))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(Dimens.SpaceM))

            // Metadata Rows
            val startedAt = info.startedAt
            if (!startedAt.isNullOrBlank()) {
                SubscriptionMetaRow(
                    icon = Icons.Rounded.CalendarToday,
                    label = stringResource(R.string.paywall_started_on, startedAt)
                )
                Spacer(modifier = Modifier.height(Dimens.SpaceS))
            }

            val renewalDate = info.renewalDate
            if (!renewalDate.isNullOrBlank() && info.cancelledAt == null && info.isActive) {
                SubscriptionMetaRow(
                    icon = Icons.Rounded.Schedule,
                    label = stringResource(R.string.paywall_renews_on, renewalDate)
                )
                Spacer(modifier = Modifier.height(Dimens.SpaceS))
            }

            val cancelledAt = info.cancelledAt
            if (!cancelledAt.isNullOrBlank()) {
                val expiryDate = renewalDate ?: cancelledAt
                SubscriptionMetaRow(
                    icon = Icons.Rounded.Schedule,
                    label = stringResource(R.string.paywall_expires_on, expiryDate)
                )
                Spacer(modifier = Modifier.height(Dimens.SpaceS))
            }

            val pendingTier = info.pendingTier
            if (!pendingTier.isNullOrBlank()) {
                val pendingTierName = pendingTier.lowercase().replaceFirstChar { it.uppercase() }
                Spacer(modifier = Modifier.height(Dimens.SpaceXS))
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(Dimens.SpaceS),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(Dimens.SpaceM),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(Dimens.IconSizeM)
                        )
                        Spacer(modifier = Modifier.width(Dimens.SpaceS))
                        Text(
                            text = stringResource(R.string.paywall_pending_tier, pendingTierName),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusPill(
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isActive) {
        CareerPilotPalette.green.copy(alpha = 0.15f)
    } else {
        CareerPilotPalette.coral.copy(alpha = 0.15f)
    }

    val textColor = if (isActive) {
        CareerPilotPalette.green
    } else {
        CareerPilotPalette.coral
    }

    val statusText = if (isActive) {
        stringResource(R.string.paywall_status_active)
    } else {
        stringResource(R.string.paywall_status_cancelled)
    }

    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(percent = 100),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Dimens.SpaceM, vertical = Dimens.SpaceXS),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(Dimens.SpaceS)
                    .clip(CircleShape)
                    .background(textColor)
            )
            Spacer(modifier = Modifier.width(Dimens.SpaceXS))
            Text(
                text = statusText,
                style = MaterialTheme.typography.labelSmall,
                color = textColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun SubscriptionMetaRow(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(Dimens.IconSizeM)
        )
        Spacer(modifier = Modifier.width(Dimens.SpaceS))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PlanBenefitsCard(
    planFeatures: List<String>,
    modifier: Modifier = Modifier
) {
    CareerPilotCard(
        elevation = Dimens.SpaceS,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.SpaceL)
        ) {
            Text(
                text = stringResource(R.string.paywall_included_features_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(Dimens.SpaceM))

            if (planFeatures.isEmpty()) {
                Text(
                    text = "No features available for this plan.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                planFeatures.forEachIndexed { index, feature ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = Dimens.SpaceXS),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = CareerPilotPalette.teal,
                            modifier = Modifier.size(Dimens.IconSizeM)
                        )
                        Spacer(modifier = Modifier.width(Dimens.SpaceM))
                        Text(
                            text = feature,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    if (index < planFeatures.lastIndex) {
                        Spacer(modifier = Modifier.height(Dimens.SpaceXS))
                    }
                }
            }
        }
    }
}

@Preview(name = "Light Mode - Plus Active", showBackground = true)
@Preview(name = "Dark Mode - Plus Active", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun MySubscriptionScreenPreview() {
    CareerPilotTheme {
        MySubscriptionScreenContent(
            state = MySubscriptionUiState(
                isLoading = false,
                subscriptionInfo = SubscriptionInfo(
                    tier = "PLUS",
                    isActive = true,
                    startedAt = "Jan 15, 2026",
                    renewalDate = "Feb 15, 2026",
                    cancelledAt = null,
                    pendingTier = null
                ),
                planFeatures = listOf(
                    "AI mock interview sessions",
                    "Unlimited voice practice mode",
                    "ATS CV scoring & optimization",
                    "CV AI analysis",
                    "Quiz-based interview practice",
                    "Export session reports as PDF"
                )
            ),
            onIntent = {}
        )
    }
}

@Preview(name = "Free Tier", showBackground = true)
@Composable
private fun MySubscriptionScreenFreePreview() {
    CareerPilotTheme {
        MySubscriptionScreenContent(
            state = MySubscriptionUiState(
                isLoading = false,
                subscriptionInfo = SubscriptionInfo(
                    tier = "FREE",
                    isActive = true,
                    startedAt = "Jan 1, 2026",
                    renewalDate = null,
                    cancelledAt = null,
                    pendingTier = null
                ),
                planFeatures = listOf(
                    "AI mock interview sessions"
                )
            ),
            onIntent = {}
        )
    }
}
