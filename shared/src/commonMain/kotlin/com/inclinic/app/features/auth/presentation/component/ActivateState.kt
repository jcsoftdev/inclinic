package com.inclinic.app.features.auth.presentation.component

import com.inclinic.app.features.auth.core.error.AuthError

data class ActivateState(
    val code: String = "",
    val isLoading: Boolean = false,
    val resendCooldownSeconds: Int = 0,
    val isSent: Boolean = false,
    val error: AuthError? = null,
)
