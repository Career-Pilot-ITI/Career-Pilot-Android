package com.iti.careerpilot.features.paywall.presentation.view.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import com.iti.careerpilot.core.designsystem.CareerPilotPalette
import com.iti.careerpilot.core.designsystem.CareerPilotShapes
import com.iti.careerpilot.core.designsystem.Dimens
import com.iti.careerpilot.core.designsystem.common.GradientIcon
import com.iti.careerpilot.core.designsystem.components.CareerPilotCard
import com.iti.careerpilot.features.paywall.domain.model.CoinPack
import com.iti.careerpilot.payment.R

@Composable
fun GetCoinsHeader(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.paywall_get_coins_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(Dimens.SpaceS))
        Box(
            modifier = Modifier
                .width(Dimens.ButtonHeight)
                .height(Dimens.WaveformBarGap)
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.secondary
                        )
                    ),
                    RoundedCornerShape(percent = 50)
                )
        )
    }
}

@Composable
fun CoinPackCard(
    pack: CoinPack,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = Dimens.SpaceM)
    ) {
        CareerPilotCard(
            elevation = Dimens.SpaceS,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = Dimens.SpaceM)
                .clickable { onSelect() }
                .border(
                    border = if (isSelected) BorderStroke(Dimens.BorderMedium, CareerPilotPalette.amber)
                    else BorderStroke(Dimens.BorderThin, Color.Transparent),
                    shape = CareerPilotShapes.medium
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.SpaceL, vertical = Dimens.SpaceL),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = CareerPilotPalette.amber,
                        modifier = Modifier.size(Dimens.SpaceXXL)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(Dimens.SpaceXXL)
                            .background(Color.Transparent, CircleShape)
                            .border(
                                width = Dimens.BorderThin,
                                color = MaterialTheme.colorScheme.outlineVariant,
                                shape = CircleShape
                            )
                    )
                }

                Spacer(modifier = Modifier.width(Dimens.SpaceM))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        GradientIcon(
                            icon = Icons.Rounded.Star,
                            modifier = Modifier.size(Dimens.IconSizeMedium).padding(bottom = Dimens.SpaceXXXS),
                            leftColor = CareerPilotPalette.yellow,
                            middleColor = CareerPilotPalette.amberLight,
                            rightColor = CareerPilotPalette.amber
                        )
                        Spacer(modifier = Modifier.width(Dimens.SpaceXS))
                        Text(
                            text = "${pack.coins}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if (pack.subtitleRes != null) {
                        Spacer(modifier = Modifier.height(Dimens.SpaceXXXS))
                        Text(
                            text = stringResource(pack.subtitleRes),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    if (pack.originalPriceEgp != null && pack.originalPriceEgp > pack.priceEgp) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val discount = pack.discountPercentage ?: 0
                            Surface(
                                color = CareerPilotPalette.green.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(percent = 100),
                                modifier = Modifier.padding(end = Dimens.SpaceS)
                            ) {
                                Text(
                                    text = stringResource(R.string.paywall_save_discount, discount),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CareerPilotPalette.green,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = Dimens.SpaceS, vertical = Dimens.SpaceXXXS)
                                )
                            }
                            Text(
                                text = stringResource(R.string.paywall_currency_egp, pack.originalPriceEgp),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.outline,
                                textDecoration = TextDecoration.LineThrough
                            )
                        }
                        Spacer(modifier = Modifier.height(Dimens.SpaceXXXS))
                    }
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = stringResource(R.string.paywall_currency_egp, pack.priceEgp),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        if (pack.badge != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = Dimens.SpaceXXL)
                    .background(
                        Brush.linearGradient(
                            listOf(CareerPilotPalette.yellow, CareerPilotPalette.amber)
                        ),
                        RoundedCornerShape(percent = 50)
                    )
            ) {
                Text(
                    text = stringResource(pack.badge.labelRes).uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(horizontal = Dimens.SpaceL, vertical = Dimens.SpaceXS)
                )
            }
        }
    }
}
