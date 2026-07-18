package com.iti.careerpilot.core.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.iti.careerpilot.core.designsystem.Dimens
import com.iti.careerpilot.core.designsystem.R

@Composable
fun NoNetworkConnectionAnimation(
    modifier: Modifier = Modifier,
    animationSize: Dp = 120.dp,
    titleColor: Color = MaterialTheme.colorScheme.onBackground.copy(
        alpha = 0.65f,
    )
) {
    val composition by rememberLottieComposition(
        spec = LottieCompositionSpec.RawRes(
            R.raw.no_internet_connection,
        ),
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        LottieAnimation(
            modifier = Modifier.size(animationSize),
            composition = composition,
            iterations = LottieConstants.IterateForever,
        )

        Spacer(modifier = Modifier.height(Dimens.SpaceM))

        Text(
            text = stringResource(R.string.no_network_connection),
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp
            ),
            color = titleColor,
            textAlign = TextAlign.Center,
        )
    }
}