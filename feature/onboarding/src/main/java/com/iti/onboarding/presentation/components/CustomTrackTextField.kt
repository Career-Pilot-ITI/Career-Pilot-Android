package com.iti.onboarding.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iti.onboarding.R

@Composable
fun CustomTrackTextField(
    value: String,
    label: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = stringResource(R.string.write_track),
) {
    val focusManager = LocalFocusManager.current
    val interactionSource = remember { MutableInteractionSource() }
    val shape = MaterialTheme.shapes.medium
    val colors = MaterialTheme.colorScheme

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium.copy(
                color = MaterialTheme.colorScheme.onBackground
            )
        )

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = modifier
                .fillMaxWidth()
                .height(58.dp)
                .shadow(
                    elevation = 10.dp,
                    shape = shape,
                    clip = true,
                    ambientColor = Color.Black.copy(alpha = 0.3f),
                    spotColor = Color.Black.copy(alpha = 0.3f),
                )
                .background(
                    color = colors.background,
                    shape = shape,
                ),
            enabled = true,
            singleLine = true,
            textStyle = TextStyle(
                color = colors.onBackground,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
            ),
            cursorBrush = SolidColor(colors.primary),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus()
                },
            ),
            interactionSource = interactionSource,
            decorationBox = { innerTextField ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Spacer(modifier = Modifier.width(20.dp))

                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        if (value.isEmpty()) {
                            Text(
                                text = placeholder,
                                color = colors.onBackground.copy(alpha = 0.3f),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Normal,
                            )
                        }

                        innerTextField()
                    }

                    Spacer(modifier = Modifier.width(20.dp))
                }
            },
        )
    }
}