package com.inclinic.app.features.auth.presentation.component

import com.inclinic.app.features.auth.core.error.AuthError

data class ResetPasswordState(
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val success: Boolean = false,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val error: AuthError? = null,
)
