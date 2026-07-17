package com.iti.common.snackbar.model

import androidx.compose.material3.SnackbarDuration
import com.iti.common.util.UIText

data class CareerPilotSnackbarEvent(
    val message: UIText,
    val type: CareerPilotSnackbarType,
    val duration: SnackbarDuration,
    val actionLabel: UIText? = null,
)
