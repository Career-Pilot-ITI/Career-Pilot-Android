package com.iti.common.snackbar

import androidx.compose.material3.SnackbarDuration
import com.iti.common.snackbar.model.CareerPilotSnackbarRequest
import com.iti.common.snackbar.model.CareerPilotSnackbarType
import com.iti.common.util.UIText
import com.iti.common.util.defaultDuration
import kotlinx.coroutines.flow.Flow

interface SnackbarController {
    val requests: Flow<CareerPilotSnackbarRequest>

    fun show(
        message: UIText,
        type: CareerPilotSnackbarType = CareerPilotSnackbarType.NORMAL,
        duration: SnackbarDuration = type.defaultDuration(),
        onAction: (() -> Unit)? = null,
    )

    fun show(
        message: UIText,
        actionLabel: UIText,
        type: CareerPilotSnackbarType = CareerPilotSnackbarType.NORMAL,
        duration: SnackbarDuration = type.defaultDuration(),
        onAction: () -> Unit,
    )
}
