package com.iti.careerpilot.core.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MonetizationOn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.iti.careerpilot.core.designsystem.CareerPilotPalette
import com.iti.careerpilot.core.designsystem.CareerPilotTheme
import com.iti.careerpilot.core.designsystem.Dimens
import com.iti.careerpilot.core.designsystem.R

/**
 * Bottom sheet shown when a user needs to purchase coins because their balance is insufficient.
 *
 * @param coinCost The cost in coins required to use the requested feature.
 * @param currentBalance The user's current coin balance.
 * @param onBuyCoins Action callback when the user taps "Buy Coins".
 * @param onDismiss Action callback when the user cancels or dismisses the bottom sheet.
 * @param sheetState ModalBottomSheet state.
 * @param modifier Modifier for styling.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoinTopUpBottomSheet(
    coinCost: Int,
    currentBalance: Int,
    onBuyCoins: () -> Unit,
    onDismiss: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    modifier: Modifier = Modifier,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier,
    ) {
        CoinTopUpBottomSheetContent(
            coinCost = coinCost,
            currentBalance = currentBalance,
            onBuyCoins = onBuyCoins,
            onDismiss = onDismiss,
        )
    }
}

/**
 * Stateless content of the coin top-up bottom sheet, displaying the coin cost breakdown
 * and buy coins CTA.
 */
@Composable
fun CoinTopUpBottomSheetContent(
    coinCost: Int,
    currentBalance: Int,
    onBuyCoins: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Icon header
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(CareerPilotPalette.amber.copy(alpha = 0.12f), shape = CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Rounded.MonetizationOn,
                contentDescription = null,
                tint = CareerPilotPalette.amber,
                modifier = Modifier.size(36.dp),
            )
        }

        // Title
        Text(
            text = stringResource(R.string.not_enough_coins_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )

        // Subtitle / Description
        Text(
            text = stringResource(R.string.coin_top_up_description),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        // Coin comparison details card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
            border = BorderStroke(
                width = Dimens.BorderThin,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
            ),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Required coins row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.coin_top_up_required),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.MonetizationOn,
                            contentDescription = null,
                            tint = CareerPilotPalette.amber,
                            modifier = Modifier.size(16.dp),
                        )
                        Text(
                            text = stringResource(R.string.pricing_badge_coins, coinCost),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                    thickness = 0.5.dp,
                )

                // Current balance row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.coin_top_up_current_balance),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.MonetizationOn,
                            contentDescription = null,
                            tint = CareerPilotPalette.gray400,
                            modifier = Modifier.size(16.dp),
                        )
                        Text(
                            text = stringResource(R.string.pricing_badge_coins, currentBalance),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(4.dp))

        // CTA Actions
        CareerPilotButton(
            text = stringResource(R.string.buy_coins),
            onClick = onBuyCoins,
            modifier = Modifier.fillMaxWidth(),
        )

        CareerPilotButton(
            text = stringResource(R.string.not_now),
            onClick = onDismiss,
            variant = ButtonVariant.GHOST,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CoinTopUpBottomSheetPreviewLight() {
    CareerPilotTheme(darkTheme = false) {
        CoinTopUpBottomSheetContent(
            coinCost = 30,
            currentBalance = 10,
            onBuyCoins = {},
            onDismiss = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0E1428)
@Composable
private fun CoinTopUpBottomSheetPreviewDark() {
    CareerPilotTheme(darkTheme = true) {
        CoinTopUpBottomSheetContent(
            coinCost = 30,
            currentBalance = 10,
            onBuyCoins = {},
            onDismiss = {},
        )
    }
}
