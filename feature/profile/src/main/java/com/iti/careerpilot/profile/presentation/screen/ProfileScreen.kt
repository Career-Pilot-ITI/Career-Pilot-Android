package com.iti.careerpilot.profile.presentation.screen

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import com.iti.common.model.ProfileEditSection
import com.iti.core.datastore.models.AccountInfo
import com.iti.core.datastore.models.CareerInfo
import com.iti.core.datastore.models.PersonalInfo
import com.iti.core.datastore.models.UserProfile
import java.io.File

@Composable
fun ProfileRoot(
    openSettings: () -> Unit,
    openEditProfile: (ProfileEditSection) -> Unit,
    openSubscription: () -> Unit = {},
    logout: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val chooserTitle = stringResource(R.string.open_cv_with)

    LaunchedEffect(viewModel) {
        viewModel.onAction(ProfileAction.Initial)
    }

    ObserveEvent(viewModel.events) { event ->
        when (event) {
            is ProfileEvent.NavigateToEditProfile -> openEditProfile(event.section)
            ProfileEvent.NavigateToSettings -> openSettings()
            ProfileEvent.NavigateToSubscription -> openSubscription()
            ProfileEvent.NavigateToLogout -> logout()
            is ProfileEvent.OpenCV -> {
                if (event.url.isNotBlank()) {
                    val uri = if (event.url.startsWith("file://")) {
                        val file = File(event.url.toUri().path!!)
                        FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            file
                        )
                    } else {
                        event.url.toUri()
                    }
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(uri, "application/pdf")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    val chooser = Intent.createChooser(intent, chooserTitle)
                    context.startActivity(chooser)
                }
            }
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
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.profile),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                ),
                windowInsets = TopAppBarDefaults.windowInsets.exclude(WindowInsets.statusBars)
            )
        },
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets.exclude(WindowInsets.navigationBars),
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp)
        ) {
            item {
                ProfileHeader(
                    profile = state.profile,
                )
            }
            item { StatsRow(profile = state.profile) }
            item {
                InfoCard(
                    profile = state.profile,
                )
            }
            item {
                CVCard(
                    fileName = state.profile.cv.cvFileName,
                    fileSize = if (state.profile.cv.cvSizeBytes > 0) "${state.profile.cv.cvSizeBytes / 1024} KB" else "",
                    onClick = {
                        val cvUri = state.profile.cv.cvLocalUri.ifBlank { state.profile.cv.cvUrl }
                        onAction(ProfileAction.OnCVClick(cvUri))
                    }
                )
            }
            item {
                MenuSection(
                    onOpenSettings = { onAction(ProfileAction.OnSettingsClick) },
                    onOpenSubscription = { onAction(ProfileAction.OnSubscriptionClick) },
                    onLogOut = { onAction(ProfileAction.OnLogoutClick) },
                    onEditPersonalInfoClick = {
                        onAction(ProfileAction.OnEditProfileClick(ProfileEditSection.PERSONAL))
                    },
                    onEditCareerClick = {
                        onAction(ProfileAction.OnEditProfileClick(ProfileEditSection.CAREER))
                    },
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
                    personal = PersonalInfo(
                        displayName = "Amina Hassan",
                    ),
                    account = AccountInfo(
                        username = "amina.h",
                        email = "amina@example.com",
                        subscriptionTier = "Max",
                        coinBalance = 240
                    ),
                    career = CareerInfo(
                        currentJobTitle = "Product Designer",
                        targetRole = "Senior Product Designer",
                        industry = "Technology",
                        experienceLevel = "Mid-level",
                    )
                )
            ),
            onAction = {}
        )
    }
}
