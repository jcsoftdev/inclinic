package com.inclinic.app.features.auth.presentation.component

import com.inclinic.app.features.auth.core.error.AuthError

data class ForgotPasswordState(
    val email: String = "",
    val isLoading: Boolean = false,
    val isSent: Boolean = false,
    val emailError: String? = null,
    val error: AuthError? = null,
)
