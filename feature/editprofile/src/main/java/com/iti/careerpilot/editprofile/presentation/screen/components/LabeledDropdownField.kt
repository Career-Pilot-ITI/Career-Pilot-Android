package com.iti.careerpilot.editprofile.presentation.screen.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.iti.careerpilot.core.designsystem.CareerPilotShapes
import com.iti.careerpilot.editprofile.R


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LabeledDropdownField(
    label: String,
    value: String,
    options: List<String>,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null
) {
    var expanded by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(if (expanded) 180f else 0f, label = "dropdownRotation")

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            leadingIcon = leadingIcon?.let { icon ->
                { Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(20.dp)) }
            },
            shape = CareerPilotShapes.medium,
            trailingIcon = {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_arrow_down),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .size(20.dp)
                        .rotate(rotation)
                )
            },
            colors = fieldColors,
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = CareerPilotShapes.medium
        ) {
            options.forEach { option ->
                val isSelected = option == value
                DropdownMenuItem(
                    text = {
                        Text(
                            text = option,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                        )
                    },
                    trailingIcon = if (isSelected) {
                        {
                            Icon(
                                imageVector = ImageVector.vectorResource(R.drawable.ic_check),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    } else null,
                    onClick = {
                        onValueChange(option)
                        expanded = false
                    },
                    modifier = Modifier
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                            else Color.Transparent
                        )
                )
            }
        }
    }
}