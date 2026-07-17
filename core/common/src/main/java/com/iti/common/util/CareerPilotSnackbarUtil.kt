package com.iti.common.util

import androidx.compose.material3.SnackbarDuration
import com.iti.common.snackbar.model.CareerPilotSnackbarType


fun CareerPilotSnackbarType.defaultDuration(): SnackbarDuration {
    return when (this) {
        CareerPilotSnackbarType.NORMAL -> SnackbarDuration.Short
        CareerPilotSnackbarType.DISMISSIBLE -> SnackbarDuration.Long
        CareerPilotSnackbarType.UNDO -> SnackbarDuration.Long
    }
}