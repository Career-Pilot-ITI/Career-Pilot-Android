package com.iti.careerpilot.core.designsystem.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.iti.careerpilot.core.designsystem.CareerPilotShapes
import com.iti.careerpilot.core.designsystem.CareerPilotTheme
import com.iti.careerpilot.core.designsystem.Dimens

enum class ButtonVariant {
    PRIMARY, SECONDARY, GHOST, OUTLINE
}

@Composable
fun CareerPilotButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: ButtonVariant = ButtonVariant.PRIMARY,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.98f else 1f, label = "buttonScale")

    val buttonModifier = modifier
        .fillMaxWidth()
        .height(Dimens.ButtonHeight)
        .scale(scale)

    val shape = CareerPilotShapes.medium
    
    when (variant) {
        ButtonVariant.PRIMARY -> {
            Button(
                onClick = onClick,
                modifier = buttonModifier,
                enabled = enabled,
                shape = shape,
                interactionSource = interactionSource,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                    disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.4f)
                )
            ) {
                Text(text = text, style = MaterialTheme.typography.labelLarge)
            }
        }
        ButtonVariant.SECONDARY -> {
            Button(
                onClick = onClick,
                modifier = buttonModifier,
                enabled = enabled,
                shape = shape,
                interactionSource = interactionSource,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                    contentColor = MaterialTheme.colorScheme.secondary,
                    disabledContainerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.04f),
                    disabledContentColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f)
                )
            ) {
                Text(text = text, style = MaterialTheme.typography.labelLarge)
            }
        }
        ButtonVariant.GHOST -> {
            TextButton(
                onClick = onClick,
                modifier = buttonModifier,
                enabled = enabled,
                shape = shape,
                interactionSource = interactionSource,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary,
                    disabledContentColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                )
            ) {
                Text(text = text, style = MaterialTheme.typography.labelLarge)
            }
        }
        ButtonVariant.OUTLINE -> {
            OutlinedButton(
                onClick = onClick,
                modifier = buttonModifier,
                enabled = enabled,
                shape = shape,
                interactionSource = interactionSource,
                border = BorderStroke(1.dp, if (enabled) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            ) {
                Text(text = text, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CareerPilotButtonPreviewLight() {
    CareerPilotTheme(darkTheme = false) {
        Column(modifier = Modifier.padding(16.dp)) {
            CareerPilotButton("Primary Button", onClick = {})
            CareerPilotButton("Disabled Primary", onClick = {}, enabled = false, modifier = Modifier.padding(top = 8.dp))
            CareerPilotButton("Secondary Button", onClick = {}, variant = ButtonVariant.SECONDARY, modifier = Modifier.padding(top = 8.dp))
            CareerPilotButton("Ghost Button", onClick = {}, variant = ButtonVariant.GHOST, modifier = Modifier.padding(top = 8.dp))
            CareerPilotButton("Outline Button", onClick = {}, variant = ButtonVariant.OUTLINE, modifier = Modifier.padding(top = 8.dp))
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0E1428)
@Composable
fun CareerPilotButtonPreviewDark() {
    CareerPilotTheme(darkTheme = true) {
        Column(modifier = Modifier.padding(16.dp)) {
            CareerPilotButton("Primary Button", onClick = {})
            CareerPilotButton("Disabled Primary", onClick = {}, enabled = false, modifier = Modifier.padding(top = 8.dp))
            CareerPilotButton("Secondary Button", onClick = {}, variant = ButtonVariant.SECONDARY, modifier = Modifier.padding(top = 8.dp))
            CareerPilotButton("Ghost Button", onClick = {}, variant = ButtonVariant.GHOST, modifier = Modifier.padding(top = 8.dp))
            CareerPilotButton("Outline Button", onClick = {}, variant = ButtonVariant.OUTLINE, modifier = Modifier.padding(top = 8.dp))
        }
    }
}
