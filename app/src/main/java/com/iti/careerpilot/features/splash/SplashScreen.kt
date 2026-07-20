package com.iti.careerpilot.features.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.iti.careerpilot.R
import com.iti.careerpilot.core.designsystem.Dimens
import com.iti.careerpilot.core.designsystem.common.ObserveEvent
import com.iti.careerpilot.core.designsystem.components.LoadingWave

@Composable
fun SplashRoot(
    onNavigateToLogin: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToOnboarding: () -> Unit,
    viewModel: SplashViewModel = hiltViewModel()
) {
    ObserveEvent(viewModel.navigationEvent) { event ->
        when (event) {
            SplashEvent.NavigateToHome -> onNavigateToHome()
            SplashEvent.NavigateToLogin -> onNavigateToLogin()
            SplashEvent.NavigateToOnboarding -> onNavigateToOnboarding()
        }
    }

    SplashScreen()
}


@Composable
fun SplashScreen(
    modifier: Modifier = Modifier,
) {
    val scale = remember { Animatable(0.5f) }

    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000),
                repeatMode = RepeatMode.Reverse
            )
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .scale(scale.value)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "CP", //todo add logo
                    color = Color.White,
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(Dimens.SpaceL))

            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(Dimens.SpaceXXL))

            LoadingWave(
                color = MaterialTheme.colorScheme.primary,
                barCount = 12,
                height = 30.dp
            )
        }
    }
}