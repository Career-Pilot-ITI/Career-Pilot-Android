package com.iti.common.snackbar

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarResult
import com.iti.common.snackbar.model.CareerPilotSnackbarRequest
import com.iti.common.snackbar.model.CareerPilotSnackbarType
import com.iti.common.util.UIText
import com.iti.common.util.defaultDuration
import kotlinx.coroutines.flow.Flow

interface SnackbarController {
    val requests: Flow<CareerPilotSnackbarRequest>

    suspend fun show(
        message: UIText,
        type: CareerPilotSnackbarType = CareerPilotSnackbarType.DISMISSIBLE,
        duration: SnackbarDuration = type.defaultDuration(),
    ): SnackbarResult

    suspend fun show(
        message: UIText,
        actionLabel: UIText,
        type: CareerPilotSnackbarType = CareerPilotSnackbarType.DISMISSIBLE,
        duration: SnackbarDuration = type.defaultDuration(),
    ): SnackbarResult
}
