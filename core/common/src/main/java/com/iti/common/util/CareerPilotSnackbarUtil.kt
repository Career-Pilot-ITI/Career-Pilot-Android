package com.iti.common.util

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarResult
import com.iti.common.snackbar.model.CareerPilotSnackbarDuration
import com.iti.common.snackbar.model.CareerPilotSnackbarResult
import com.iti.common.snackbar.model.CareerPilotSnackbarType


val CareerPilotSnackbarType.isDismissible: Boolean
    get() = this == CareerPilotSnackbarType.DISMISSIBLE

fun CareerPilotSnackbarDuration.toMaterialDuration(): SnackbarDuration {
    return when (this) {
        CareerPilotSnackbarDuration.SHORT -> SnackbarDuration.Short
        CareerPilotSnackbarDuration.LONG -> SnackbarDuration.Long
    }
}

fun SnackbarResult.toCareerPilotResult(): CareerPilotSnackbarResult {
    return when (this) {
        SnackbarResult.ActionPerformed -> CareerPilotSnackbarResult.ACTION_PERFORMED
        SnackbarResult.Dismissed -> CareerPilotSnackbarResult.DISMISSED
    }
}