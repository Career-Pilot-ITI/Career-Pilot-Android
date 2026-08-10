package com.iti.careerpilot.ats.presentation.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.iti.careerpilot.ats.R
import com.iti.careerpilot.ats.presentation.entry.AtsEntryRoot
import com.iti.careerpilot.ats.presentation.jobdetails.JobDetailsRoot
import com.iti.careerpilot.ats.presentation.scoring.ScoringRoot

@Composable
fun AtsNavDisplay(
    initialSharedText: String?,
    onSharedTextConsumed: () -> Unit,
    onBottomBarVisibilityChanged: (Boolean) -> Unit,
    openCoinsPaywall: () -> Unit,
    openPracticeSession: (trackId: Long) -> Unit,
) {
    val backStack = rememberNavBackStack(AtsRoute.Entry)
    val currentRoute = backStack.lastOrNull()

    LaunchedEffect(currentRoute) {
        onBottomBarVisibilityChanged(currentRoute == AtsRoute.Entry)
    }

    NavDisplay(
        modifier = Modifier.fillMaxSize(),
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = entryProvider {
            entry<AtsRoute.Entry> {
                AtsEntryRoot(
                    initialSharedText = initialSharedText,
                    onSharedTextConsumed = onSharedTextConsumed,
                    onWorkspaceImported = { workspaceId ->
                        backStack.add(AtsRoute.JobDetails(workspaceId))
                    },
                )
            }
            entry<AtsRoute.JobDetails> { route ->
                JobDetailsRoot(
                    workspaceId = route.workspaceId,
                    onBack = { backStack.removeLastOrNull() },
                    onStartScoring = { workspaceId -> backStack.add(AtsRoute.Score(workspaceId)) },
                )
            }
            entry<AtsRoute.Score> { route ->
                ScoringRoot(
                    workspaceId = route.workspaceId,
                    onBack = { backStack.removeLastOrNull() },
                    openCoinsPaywall = openCoinsPaywall,
                    openCoverLetter = { workspaceId -> backStack.add(AtsRoute.CoverLetter(workspaceId)) },
                    openOptimizedCv = { workspaceId -> backStack.add(AtsRoute.OptimizedCv(workspaceId)) },
                    openPracticeSession = openPracticeSession,
                )
            }
            entry<AtsRoute.CoverLetter> {
                AtsPlaceholder(title = stringResource(R.string.cover_letter))
            }
            entry<AtsRoute.OptimizedCv> {
                AtsPlaceholder(title = stringResource(R.string.optimized_cv))
            }
        },
    )
}

@Composable
private fun AtsPlaceholder(
    title: String,
    onContinue: (() -> Unit)? = null,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(title)
        onContinue?.let { action ->
            Button(onClick = action) { Text(stringResource(R.string.continue_action)) }
        }
    }
}
