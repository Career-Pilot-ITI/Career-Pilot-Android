package com.iti.careerpilot.features.settings.presentation.componnents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.iti.careerpilot.R
import com.iti.careerpilot.core.designsystem.common.GradientIcon
import com.iti.careerpilot.features.settings.domain.models.ThemeSetting

@Composable
fun ThemeDialog(
    onDismissRequest: () -> Unit,
    setTheme: (ThemeSetting) -> Unit,
    selectedTheme: ThemeSetting
) {
    Dialog(
        onDismissRequest = {
            onDismissRequest()
        }
    ) {
        OutlinedCard {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .padding(start = 20.dp, top = 20.dp)
            ) {
                GradientIcon(
                    icon = ImageVector.vectorResource(R.drawable.ic_theme),
                    modifier = Modifier
                )
                Text(
                    text = stringResource(R.string.select_theme),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
            }
            LazyColumn(
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                items(ThemeSetting.entries) { themeSetting ->
                    val iconId = when (themeSetting) {
                        ThemeSetting.LIGHT -> R.drawable.ic_light_mode
                        ThemeSetting.DARK -> R.drawable.ic_dark_mode
                        ThemeSetting.FOLLOW_SYSTEM -> R.drawable.ic_follow_system
                    }
                    ElevatedButton(
                        onClick = {
                            setTheme(themeSetting)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = iconId),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(
                            modifier = Modifier.width(8.dp)
                        )
                        Text(
                            text = stringResource(themeSetting.getTitleId()),
                            style = MaterialTheme.typography.headlineSmall,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(
                            modifier = Modifier.weight(1f)
                        )
                        if (selectedTheme == themeSetting) {
                            Icon(
                                imageVector = ImageVector.vectorResource(id = R.drawable.ic_check),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }
            Row(
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(start = 20.dp, end = 20.dp, bottom = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        onDismissRequest()
                    },
                    modifier = Modifier
                        .weight(1f)
                ) {
                    Text(
                        text = stringResource(R.string.done),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}