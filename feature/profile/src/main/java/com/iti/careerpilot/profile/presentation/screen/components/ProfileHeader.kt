package com.iti.careerpilot.profile.presentation.screen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.iti.careerpilot.core.designsystem.CareerPilotPalette
import com.iti.careerpilot.core.designsystem.components.ButtonVariant
import com.iti.careerpilot.core.designsystem.components.CareerPilotButton
import com.iti.careerpilot.core.designsystem.components.CareerPilotCard
import com.iti.careerpilot.profile.R
import com.iti.core.datastore.models.UserProfile


@Composable
fun ProfileHeader(
    profile: UserProfile,
    onEditClick: () -> Unit
) {
    CareerPilotCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(84.dp)
                    .clip(CircleShape)
                    .background(CareerPilotPalette.gray100)
            ) {
                if (profile.avatarUrl.isNotBlank()) {
                    AsyncImage(
                        model = profile.avatarUrl,
                        contentDescription = stringResource(R.string.profile_avatar),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                    )
                } else {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.ic_account),
                        contentDescription = null,
                        tint = CareerPilotPalette.gray400,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(40.dp)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = profile.displayName.ifBlank { stringResource(R.string.add_your_name) },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (profile.username.isNotBlank()) {
                Text(
                    text = stringResource(R.string.username, profile.username),
                    style = MaterialTheme.typography.bodyMedium,
                    color = CareerPilotPalette.gray600
                )
            }

            if (profile.currentJobTitle.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = profile.currentJobTitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = CareerPilotPalette.gray600,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.height(16.dp))

            CareerPilotButton(
                text = stringResource(R.string.edit_profile),
                onClick = onEditClick,
                variant = ButtonVariant.OUTLINE,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}