package com.inclinic.app.features.auth.presentation.component

import com.inclinic.app.features.auth.core.error.AuthError

data class RegisterPatientState(
    val name: String = "",
    val lastName: String = "",
    val email: String = "",
    val phone: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val nameError: String? = null,
    val lastNameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val serverError: AuthError? = null,
    val success: Boolean = false,
)
