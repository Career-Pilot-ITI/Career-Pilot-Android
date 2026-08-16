package com.iti.careerpilot.profile.presentation.screen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.iti.careerpilot.core.designsystem.common.GradientIcon
import com.iti.careerpilot.core.designsystem.components.CareerPilotCard
import com.iti.careerpilot.profile.R
import com.iti.core.datastore.models.UserProfile


@Composable
fun ProfileHeader(
    profile: UserProfile,
) {
    CareerPilotCard(
        elevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .background(
                        Brush.linearGradient(
                            0f to MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                            0.5f to MaterialTheme.colorScheme.tertiary.copy(alpha = 0.18f),
                            1f to MaterialTheme.colorScheme.secondary.copy(alpha = 0.18f),
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(28.dp))

                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .border(3.dp, MaterialTheme.colorScheme.surface, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (profile.avatar.avatarLocalUri.isNotBlank()) {
                        AsyncImage(
                            model = profile.avatar.avatarLocalUri,
                            contentDescription = stringResource(R.string.profile_avatar),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            GradientIcon(
                                icon = ImageVector.vectorResource(id = R.drawable.ic_account),
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                Text(
                    text = profile.personal.displayName.ifBlank { stringResource(R.string.add_your_name) },
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                if (profile.account.username.isNotBlank()) {
                    Text(
                        text = stringResource(R.string.username, profile.account.username),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (profile.career.currentJobTitle.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = profile.career.currentJobTitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
