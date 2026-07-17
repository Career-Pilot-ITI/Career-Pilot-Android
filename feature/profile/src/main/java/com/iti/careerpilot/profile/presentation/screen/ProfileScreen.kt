package com.iti.careerpilot.profile.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iti.common.model.ProfileEditSection
import com.iti.careerpilot.core.designsystem.CareerPilotTheme
import com.iti.careerpilot.core.designsystem.common.ObserveEvent
import com.iti.careerpilot.profile.R
import com.iti.careerpilot.profile.presentation.action.ProfileAction
import com.iti.careerpilot.profile.presentation.event.ProfileEvent
import com.iti.careerpilot.profile.presentation.screen.components.CVCard
import com.iti.careerpilot.profile.presentation.screen.components.InfoCard
import com.iti.careerpilot.profile.presentation.screen.components.LogoutDialog
import com.iti.careerpilot.profile.presentation.screen.components.MenuSection
import com.iti.careerpilot.profile.presentation.screen.components.ProfileHeader
import com.iti.careerpilot.profile.presentation.screen.components.StatsRow
import com.iti.careerpilot.profile.presentation.state.ProfileState
import com.iti.careerpilot.profile.presentation.viewmodel.ProfileViewModel
import com.iti.core.datastore.models.UserProfile

@Composable
fun ProfileRoot(
    openSettings: () -> Unit,
    openEditProfile: (ProfileEditSection) -> Unit,
    logout: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveEvent(viewModel.events) { event ->
        when (event) {
            is ProfileEvent.NavigateToEditProfile -> openEditProfile(event.section)
            ProfileEvent.NavigateToSettings -> openSettings()
            ProfileEvent.NavigateToLogout -> logout()
        }
    }

    ProfileScreen(
        state = state,
        onAction = viewModel::onAction
    )
}

@Composable
fun ProfileScreen(
    state: ProfileState,
    onAction: (ProfileAction) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        TopAppBar(
            title = {
                Text(
                    text = stringResource(R.string.profile),
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent
            ),
            windowInsets = TopAppBarDefaults.windowInsets.exclude(WindowInsets.statusBars)
        )
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(20.dp)
        ) {

            item {
                ProfileHeader(
                    profile = state.profile,
                    onEditPersonalInfoClick = { onAction(ProfileAction.OnEditProfileClick(ProfileEditSection.PERSONAL)) }
                )
            }

            item {
                StatsRow(profile = state.profile)
            }

            item {
                InfoCard(
                    profile = state.profile,
                    onEditCareerClick = { onAction(ProfileAction.OnEditProfileClick(ProfileEditSection.CAREER)) }
                )
            }

            item {
                CVCard(
                    fileName = state.profile.cvFileName,
                    fileSize = if (state.profile.cvSizeBytes > 0) "${state.profile.cvSizeBytes / 1024} KB" else ""
                )
            }

            item {
                MenuSection(
                    onOpenSettings = {
                        onAction(ProfileAction.OnSettingsClick)
                    },
                    onLogOut = {
                        onAction(ProfileAction.OnLogoutClick)
                    }
                )
            }
        }
    }

    if (state.showLogoutDialog) {
        LogoutDialog(
            onConfirm = { onAction(ProfileAction.OnLogoutConfirm) },
            onDismiss = { onAction(ProfileAction.OnLogoutDismiss) }
        )
    }
}


@Preview(showBackground = true)
@Composable
private fun ProfileScreenPreview() {
    CareerPilotTheme {
        ProfileScreen(
            state = ProfileState(
                profile = UserProfile(
                    displayName = "Amina Hassan",
                    username = "amina.h",
                    email = "amina@example.com",
                    currentJobTitle = "Product Designer",
                    targetRole = "Senior Product Designer",
                    industry = "Technology",
                    experienceLevel = "Mid-level",
                    subscriptionTier = "Pro",
                    coinBalance = 240
                )
            ),
            onAction = {}
        )
    }
}