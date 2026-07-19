package com.iti.careerpilot.login.presentation.login

import com.iti.common.util.UIText

data class LoginState(
    val phoneNumber: String = "",
    val regionCode: String = "",
    val isLoading: Boolean = false,
    val error: UIText? = null,
    val cooldownSecondsRemaining: Int = 0,
) {
    val isInCooldown: Boolean get() = cooldownSecondsRemaining > 0
}
