package com.iti.careerpilot.core.designsystem.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.iti.careerpilot.core.designsystem.Dimens

@Composable
fun CareerPilotAppScaffold(
    isOnline: Boolean,
    snackbarHostState: SnackbarHostState,
    hasBottomNavigationBar: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        contentWindowInsets = WindowInsets(0, 0, 0, 0), // keep 0 and handle padding in every screen

        topBar = {
            AnimatedVisibility(
                visible = !isOnline,
            ) {
                OfflineModeTopBar(
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        snackbarHost = {
            CareerPilotSnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .padding(horizontal = Dimens.SpaceL)
                    .padding(
                        bottom = if (hasBottomNavigationBar) {
                            Dimens.BottomNavHeight + Dimens.SpaceM
                        } else {
                            Dimens.SpaceM
                        },
                    )
                    .navigationBarsPadding(),
            )
        },
        content = content,
    )
}