package com.iti.onboarding.presentation.screen.profileinfo.view

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iti.careerpilot.core.designsystem.components.LoadingDialog
import com.iti.careerpilot.core.designsystem.components.UploadProgressDialog
import com.iti.careerpilot.core.designsystem.components.media.AvatarImagePicker
import com.iti.careerpilot.core.designsystem.components.media.ImageSourcePickerSheet
import com.iti.careerpilot.core.designsystem.R as DesignSystemR
import com.iti.common.media.ImageSource
import com.iti.common.media.rememberImagePickerLauncher
import com.iti.common.snackbar.CareerPilotSnackbarController
import androidx.compose.ui.res.stringResource
import com.iti.onboarding.R
import com.iti.onboarding.presentation.screen.profileinfo.view.components.AddSkillDialog
import com.iti.onboarding.presentation.screen.profileinfo.view.components.ProfileBanner
import com.iti.onboarding.presentation.screen.profileinfo.view.components.ProfileHeader
import com.iti.onboarding.presentation.screen.profileinfo.view.components.ProfileInfoForm
import com.iti.onboarding.presentation.screen.profileinfo.view.components.ProfileSkillsSection
import com.iti.onboarding.presentation.screen.profileinfo.view.components.ProfileTitle
import com.iti.onboarding.presentation.screen.profileinfo.viewmodel.ProfileInfoEffect
import com.iti.onboarding.presentation.screen.profileinfo.viewmodel.ProfileInfoIntent
import com.iti.onboarding.presentation.screen.profileinfo.viewmodel.ProfileInfoUiState
import com.iti.onboarding.presentation.screen.profileinfo.viewmodel.ProfileInfoViewModel
import kotlinx.coroutines.flow.Flow

@ExperimentalMaterial3ExpressiveApi
@Composable
fun ProfileInfoScreen(
    viewModel: ProfileInfoViewModel = hiltViewModel(),
    onNavigateNext: () -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ProfileScreenContent(
        state = state,
        effectFlow = viewModel.effect,
        onIntent = viewModel::onIntent,
        onNavigateNext = onNavigateNext
    )
}

@ExperimentalMaterial3ExpressiveApi
@Composable
fun ProfileScreenContent(
    state: ProfileInfoUiState,
    effectFlow: Flow<ProfileInfoEffect>,
    onIntent: (ProfileInfoIntent) -> Unit,
    onNavigateNext: () -> Unit = {}
) {
    val data = state.data
    val isImageUploading = state.isImageUploading
    val isImageSourceSheetVisible = state.isImageSourceSheetVisible
    val context = LocalContext.current

    val imagePicker = rememberImagePickerLauncher(
        onImagePicked = { uri -> onIntent(ProfileInfoIntent.OnImagePicked(uri)) },
        onCameraPermissionGranted = { onIntent(ProfileInfoIntent.OnImageSourceSelected(ImageSource.CAMERA)) },
        onCameraPermissionDenied = { onIntent(ProfileInfoIntent.OnCameraPermissionDenied) }
    )

    @Suppress("LocalContextGetResourceValueCall")
    LaunchedEffect(Unit) {
        effectFlow.collect { effect ->
            when (effect) {
                is ProfileInfoEffect.LaunchCamera -> {
                    imagePicker.launchCamera(effect.captureUri)
                }

                is ProfileInfoEffect.RequestCameraPermission -> {
                    imagePicker.requestCameraPermission()
                }

                is ProfileInfoEffect.OpenAppSettings -> {
                    context.startActivity(
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            .setData(Uri.fromParts("package", context.packageName, null))
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                }

                is ProfileInfoEffect.NavigateToNextScreen -> {
                    onNavigateNext()
                }
            }
        }
    }

    val focusManager = LocalFocusManager.current

    val defaultSkills = stringArrayResource(id = R.array.profile_info_default_skills).toList()

    LaunchedEffect(defaultSkills) {
        onIntent(ProfileInfoIntent.OnInitDefaultSkills(defaultSkills))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
                .padding(bottom = 120.dp), // Extra padding for the global floating button
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ProfileHeader(modifier = Modifier.align(Alignment.Start))
            Spacer(modifier = Modifier.height(16.dp))
            ProfileTitle(modifier = Modifier.align(Alignment.Start))
            Spacer(modifier = Modifier.height(24.dp))

            AvatarImagePicker(
                imageUrl = data.selectedImageUri ?: data.avatarUrl,
                isUploading = isImageUploading,
                onClick = { onIntent(ProfileInfoIntent.OnAvatarClicked) },
                name = data.name.takeIf { it.isNotBlank() }
                    ?: androidx.compose.ui.res.stringResource(id = R.string.profile_info_full_name_placeholder)
            )

            Spacer(modifier = Modifier.height(24.dp))

            ProfileInfoForm(
                name = data.name,
                onNameChanged = { onIntent(ProfileInfoIntent.OnNameChanged(it)) },
                email = data.email,
                onEmailChanged = { onIntent(ProfileInfoIntent.OnEmailChanged(it)) },
                isEmailInvalid = state.isEmailInvalid,
                title = data.title,
                onTitleChanged = { onIntent(ProfileInfoIntent.OnTitleChanged(it)) },
                experience = data.experience,
                onExperienceChanged = { onIntent(ProfileInfoIntent.OnExperienceChanged(it)) },
                focusManager = focusManager
            )

            Spacer(modifier = Modifier.height(24.dp))

            ProfileSkillsSection(
                skills = data.skills,
                allSkills = state.allSkills,
                onSkillsChanged = { onIntent(ProfileInfoIntent.OnSkillsChanged(it)) },
                onAddSkillClicked = { onIntent(ProfileInfoIntent.OnShowAddSkillDialogChanged(true)) },
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(24.dp))

            ProfileBanner()

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (state.showAddSkillDialog) {
        AddSkillDialog(
            newSkillText = state.newSkillText,
            onNewSkillTextChanged = { onIntent(ProfileInfoIntent.OnNewSkillTextChanged(it)) },
            onConfirm = { onIntent(ProfileInfoIntent.OnConfirmAddSkill) },
            onDismiss = {
                onIntent(ProfileInfoIntent.OnNewSkillTextChanged(""))
                onIntent(ProfileInfoIntent.OnShowAddSkillDialogChanged(false))
            }
        )
    }

    if (isImageSourceSheetVisible) {
        ImageSourcePickerSheet(
            onTakePhoto = {
                if (ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    onIntent(ProfileInfoIntent.OnImageSourceSelected(ImageSource.CAMERA))
                } else {
                    imagePicker.requestCameraPermission()
                }
            },
            onChooseFromGallery = {
                onIntent(ProfileInfoIntent.OnImageSourceSelected(ImageSource.GALLERY))
                imagePicker.launchGallery()
            },
            onDismissRequest = {
                onIntent(ProfileInfoIntent.OnDismissSheet)
            }
        )
    }

    if (state.isSubmitting) {
        LoadingDialog()
    }

    if (isImageUploading) {
        UploadProgressDialog(
            progress = state.uploadProgress,
            title = stringResource(DesignSystemR.string.uploading)
        )
    }
}
