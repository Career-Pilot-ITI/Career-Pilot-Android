package com.iti.careerpilot.core.designsystem.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.iti.careerpilot.core.designsystem.CareerPilotShapes
import com.iti.careerpilot.core.designsystem.CareerPilotTheme
import com.iti.careerpilot.core.designsystem.Dimens
import com.iti.careerpilot.core.designsystem.softShadow

@Composable
fun CareerPilotCard(
    modifier: Modifier = Modifier,
    useShadow: Boolean = true,
    content: @Composable () -> Unit
) {
    Card(
        modifier = if (useShadow) modifier.softShadow() else modifier,
        shape = CareerPilotShapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        content()
    }
}

@Preview(showBackground = true)
@Composable
fun CareerPilotCardPreviewLight() {
    CareerPilotTheme(darkTheme = false) {
        Column(modifier = Modifier.padding(16.dp)) {
            CareerPilotCard {
                Text(
                    text = "Light Card Content",
                    modifier = Modifier.padding(Dimens.CardPadding)
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0E1428)
@Composable
fun CareerPilotCardPreviewDark() {
    CareerPilotTheme(darkTheme = true) {
        Column(modifier = Modifier.padding(16.dp)) {
            CareerPilotCard {
                Text(
                    text = "Dark Card Content",
                    modifier = Modifier.padding(Dimens.CardPadding)
                )
            }
        }
    }
}
