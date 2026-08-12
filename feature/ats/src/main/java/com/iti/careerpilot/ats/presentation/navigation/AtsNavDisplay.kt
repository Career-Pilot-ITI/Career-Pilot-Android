package com.iti.careerpilot.ats.presentation.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.iti.careerpilot.ats.presentation.entry.view.AtsEntryRoot
import com.iti.careerpilot.ats.presentation.scoring.ScoringRoot
import com.iti.careerpilot.ats.presentation.coverletter.CoverLetterRoot
import com.iti.careerpilot.ats.presentation.optimizedcv.OptimizedCvRoot

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
                    onScoreRequested = { workspaceId ->
                        backStack.add(AtsRoute.Score(workspaceId))
                    },
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
            entry<AtsRoute.CoverLetter> { route ->
                CoverLetterRoot(
                    workspaceId = route.workspaceId,
                    onBack = { backStack.removeLastOrNull() },
                    openCoinsPaywall = openCoinsPaywall,
                )
            }
            entry<AtsRoute.OptimizedCv> { route ->
                OptimizedCvRoot(
                    workspaceId = route.workspaceId,
                    onBack = { backStack.removeLastOrNull() },
                    openCoinsPaywall = openCoinsPaywall,
                )
            }
        },
    )
}
