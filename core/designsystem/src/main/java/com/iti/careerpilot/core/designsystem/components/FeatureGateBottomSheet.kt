package com.iti.careerpilot.core.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.iti.core.model.Plan

/**
 * Bottom sheet shown when a user taps a feature locked by their subscription plan.
 *
 * @param featureName    Short name for the locked feature, e.g. "Video Interview".
 * @param requiredPlan   Minimum [Plan] needed — used for the display name in copy.
 * @param planFeatures   What the user gains by upgrading. Compute in the ViewModel:
 *                       `PlanAccessMap.featuresFor(requiredPlan).map { it.displayName() }`
 * @param onUpgradeClick Called when user taps "Upgrade to [Plan]".
 * @param onDismiss      Called when user taps "Not now" or swipes the sheet down.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeatureGateBottomSheet(
    featureName: String,
    requiredPlan: Plan,
    planFeatures: List<String>,
    onUpgradeClick: () -> Unit,
    onDismiss: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    modifier: Modifier = Modifier,
) {
    val planDisplayName = requiredPlan.displayName()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Icon(
                imageVector = Icons.Rounded.Lock,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp),
            )
            Text(
                text = androidx.compose.ui.res.stringResource(com.iti.careerpilot.core.designsystem.R.string.upgrade_to_plan, planDisplayName),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            Text(
                text = androidx.compose.ui.res.stringResource(
                    com.iti.careerpilot.core.designsystem.R.string.feature_gate_subtitle,
                    featureName,
                    planDisplayName
                ),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            // Dynamic feature list — driven by PlanAccessMap via the caller
            if (planFeatures.isNotEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    planFeatures.forEach { feature ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp),
                            )
                            Text(
                                text = feature,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(4.dp))

            Button(onClick = onUpgradeClick, modifier = Modifier.fillMaxWidth()) {
                Text(androidx.compose.ui.res.stringResource(com.iti.careerpilot.core.designsystem.R.string.upgrade_to_plan, planDisplayName))
            }
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text(androidx.compose.ui.res.stringResource(com.iti.careerpilot.core.designsystem.R.string.not_now))
            }
        }
    }
}
