package com.iti.careerpilot.ats.presentation.coverletter.view.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.iti.careerpilot.ats.R
import com.iti.careerpilot.core.designsystem.components.CareerPilotCard

@Composable
internal fun ApproachTipsCard(
    tips: String,
    modifier: Modifier = Modifier,
) {
    CareerPilotCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.ats_approach_tips),
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = tips,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}
