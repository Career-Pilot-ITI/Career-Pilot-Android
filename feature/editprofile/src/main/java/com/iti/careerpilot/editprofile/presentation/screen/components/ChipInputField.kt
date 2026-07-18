package com.iti.careerpilot.editprofile.presentation.screen.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.iti.careerpilot.core.designsystem.CareerPilotShapes
import com.iti.careerpilot.editprofile.R


@Composable
fun ChipInputField(
    label: String,
    chips: List<String>,
    placeholder: String,
    onAdd: (String) -> Unit,
    onRemove: (String) -> Unit,
    leadingIcon: ImageVector? = null
) {
    var input by remember { mutableStateOf("") }

    Column {
        OutlinedTextField(
            value = input,
            onValueChange = { input = it },
            label = { Text(label) },
            placeholder = { Text(placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant) },
            leadingIcon = leadingIcon?.let { icon ->
                { Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp)) }
            },
            singleLine = true,
            shape = CareerPilotShapes.medium,
            trailingIcon = {
                AnimatedVisibility(
                    visible = input.isNotBlank(),
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut()
                ) {
                    IconButton(onClick = { onAdd(input); input = "" }) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(
                                imageVector = ImageVector.vectorResource(R.drawable.ic_add),
                                contentDescription = stringResource(R.string.add),
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = { if (input.isNotBlank()) { onAdd(input); input = "" } }
            ),
            colors = fieldColors,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        if (chips.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                chips.forEach { chip -> Chip(text = chip, onRemove = { onRemove(chip) }) }
            }
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(CareerPilotShapes.medium)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                        shape = CareerPilotShapes.medium
                    )
                    .padding(vertical = 10.dp, horizontal = 12.dp)
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_star),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.nothing_added_yet),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun Chip(text: String, onRemove: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.16f)
                    )
                )
            )
            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f), RoundedCornerShape(50))
            .padding(start = 12.dp, end = 6.dp, top = 7.dp, bottom = 7.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.width(6.dp))
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                .clickable(onClick = onRemove)
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_close),
                contentDescription = stringResource(R.string.remove_x, text),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(12.dp)
            )
        }
    }
}
