package com.iti.careerpilot.core.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.TextButton
import androidx.wear.compose.material3.TextButtonDefaults
import com.iti.careerpilot.core.designsystem.Dimens
import com.iti.careerpilot.core.designsystem.R

@Composable
fun CareerPilotSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    SnackbarHost(
        hostState = hostState,
        modifier = modifier,
    ) { snackbarData ->
        CareerPilotSnackbar(
            snackbarData = snackbarData,
        )
    }
}

@Composable
private fun CareerPilotSnackbar(
    snackbarData: SnackbarData,
    modifier: Modifier = Modifier,
) {
    val visuals = snackbarData.visuals

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.inverseSurface,
        contentColor = MaterialTheme.colorScheme.inverseOnSurface,
        shadowElevation = 6.dp,
    ) {
        Row(
            modifier = Modifier
                .height(56.dp)
                .padding(end = Dimens.SpaceS, start = Dimens.CardPadding),
            horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceXS),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = visuals.message,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.inverseOnSurface,
            )

            visuals.actionLabel?.let { actionLabel ->
                TextButton(
                    onClick = snackbarData::performAction,
                    colors = TextButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.inversePrimary,
                    ),
                ) {
                    Text(
                        text = actionLabel.toUpperCase(Locale.current),
                        style = MaterialTheme.typography.labelMedium.copy(
                            textDecoration = TextDecoration.Underline
                        ),
                    )
                }
            }

            if (visuals.withDismissAction) {
                IconButton(
                    onClick = snackbarData::dismiss
                ) {
                    Icon(
                        modifier = Modifier.size(20.dp),
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(
                            R.string.snackbar_dismiss_action,
                        ),
                        tint = MaterialTheme.colorScheme.inverseOnSurface,
                    )
                }
            }
        }
    }
}
