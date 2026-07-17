package com.iti.common.snackbar.model

import com.iti.common.util.UIText

data class CareerPilotSnackbarEvent(
    val message: UIText,
    val type: CareerPilotSnackbarType,
    val duration: CareerPilotSnackbarDuration,
    val actionLabel: UIText? = null,
)
