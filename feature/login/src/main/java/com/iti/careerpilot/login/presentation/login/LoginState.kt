package com.iti.careerpilot.login.presentation.login

import com.iti.common.util.UIText

data class LoginState(
    val phoneNumber: String = "",
    val regionCode: String = "",
    val isLoading: Boolean = false,
    val error: UIText? = null,
)
