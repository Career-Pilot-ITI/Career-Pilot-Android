package com.iti.careerpilot.core.designsystem.components.media

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.iti.careerpilot.core.designsystem.R

import androidx.compose.material3.Text
import androidx.compose.ui.text.font.FontWeight

@androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
@Composable
fun AvatarImagePicker(
    imageUrl: Any?,
    isUploading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    name: String? = null
) {
    Box(
        modifier = modifier
            .size(96.dp)
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer)
                .clickable(enabled = !isUploading, onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            if (imageUrl != null && imageUrl.toString().isNotBlank()) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = stringResource(id = R.string.avatar_picker_content_description),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else if (!name.isNullOrBlank()) {
                val initials = name.trim().split("\\s+".toRegex())
                    .take(2)
                    .mapNotNull { it.firstOrNull()?.uppercase() }
                    .joinToString("")
                Text(
                    text = initials,
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = stringResource(id = R.string.avatar_picker_content_description),
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            
            if (isUploading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.material3.LoadingIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
        
        if (!isUploading) {
            FilledIconButton(
                onClick = onClick,
                modifier = Modifier
                    .size(32.dp)
                    .align(Alignment.BottomEnd)
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = stringResource(id = R.string.avatar_picker_add_photo),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
