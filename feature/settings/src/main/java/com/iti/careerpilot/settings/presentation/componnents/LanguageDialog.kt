package com.iti.careerpilot.settings.presentation.componnents

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.os.LocaleListCompat
import com.iti.careerpilot.core.designsystem.common.GradientIcon
import com.iti.careerpilot.settings.R
import com.iti.careerpilot.settings.presentation.viewmodel.getCode
import com.iti.careerpilot.settings.presentation.viewmodel.getTitleId
import com.iti.core.datastore.settings.domain.models.LanguageSetting

@Composable
fun LanguageDialog(
    onDismissRequest: () -> Unit,
    initial: LanguageSetting
) {
    Dialog(
        onDismissRequest = {
            onDismissRequest()
        }
    ) {
        var selectedLang by remember {
            mutableStateOf(initial)
        }
        OutlinedCard {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .padding(start = 20.dp, top = 20.dp)
            ) {
                GradientIcon(
                    icon = ImageVector.vectorResource(R.drawable.ic_language),
                    modifier = Modifier
                )
                Text(
                    text = stringResource(R.string.select_language),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
            }
            LazyColumn(
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                items(LanguageSetting.entries) { languageSetting ->
                    ElevatedButton(
                        onClick = {
                            selectedLang = languageSetting
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = stringResource(languageSetting.getTitleId()),
                            style = MaterialTheme.typography.headlineSmall,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(
                            modifier = Modifier.weight(1f)
                        )
                        if (selectedLang == languageSetting) {
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
                TextButton(
                    onClick = {
                        onDismissRequest()
                    },
                    modifier = Modifier
                        .weight(1f)
                ) {
                    Text(
                        text = stringResource(R.string.cancel),
                        fontWeight = FontWeight.Bold
                    )
                }
                Button(
                    onClick = {
                        onDismissRequest()
                        changeLanguage(selectedLang.getCode())
                    },
                    modifier = Modifier
                        .weight(1f)
                ) {
                    Text(
                        text = stringResource(R.string.confirm),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

private fun changeLanguage(langCode: String) {
    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(langCode))
}