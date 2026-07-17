package com.iti.common.snackbar.usecase

import com.iti.common.R
import com.iti.common.snackbar.SnackbarController
import com.iti.common.snackbar.model.CareerPilotSnackbarDuration
import com.iti.common.snackbar.model.CareerPilotSnackbarEvent
import com.iti.common.snackbar.model.CareerPilotSnackbarResult
import com.iti.common.snackbar.model.CareerPilotSnackbarType
import com.iti.common.util.UIText
import javax.inject.Inject

class ShowSnackbarUseCase @Inject constructor(
    private val snackbarController: SnackbarController,
) {
    suspend operator fun invoke(
        message: UIText,
        type: CareerPilotSnackbarType = CareerPilotSnackbarType.NORMAL,
        duration: CareerPilotSnackbarDuration = type.defaultDuration(),
    ): CareerPilotSnackbarResult {
        val actionLabel = when (type) {
            CareerPilotSnackbarType.UNDO ->
                UIText.StringResource(R.string.snackbar_action_undo)

            CareerPilotSnackbarType.NORMAL,
            CareerPilotSnackbarType.DISMISSIBLE -> null
        }

        return snackbarController.show(
            event = CareerPilotSnackbarEvent(
                message = message,
                type = type,
                duration = duration,
                actionLabel = actionLabel,
            ),
        )
    }
}

private fun CareerPilotSnackbarType.defaultDuration(): CareerPilotSnackbarDuration {
    return when (this) {
        CareerPilotSnackbarType.NORMAL -> CareerPilotSnackbarDuration.SHORT
        CareerPilotSnackbarType.DISMISSIBLE -> CareerPilotSnackbarDuration.LONG
        CareerPilotSnackbarType.UNDO -> CareerPilotSnackbarDuration.LONG
    }
}
