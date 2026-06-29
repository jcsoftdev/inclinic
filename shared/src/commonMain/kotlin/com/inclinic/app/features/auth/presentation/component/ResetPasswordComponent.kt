package com.inclinic.app.features.auth.presentation.component

import com.arkivanov.decompose.value.Value

interface ResetPasswordComponent {
    val token: String
    val state: Value<ResetPasswordState>

    fun onPasswordChanged(password: String)
    fun onConfirmPasswordChanged(confirmPassword: String)
    fun onSubmit()
    fun onBack()

    sealed interface Output {
        data object Success : Output
        data object Back : Output
    }
}
