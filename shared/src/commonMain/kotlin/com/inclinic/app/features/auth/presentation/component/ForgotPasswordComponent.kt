package com.inclinic.app.features.auth.presentation.component

import com.arkivanov.decompose.value.Value

interface ForgotPasswordComponent {
    val state: Value<ForgotPasswordState>

    fun onEmailChanged(email: String)
    fun onSubmit()
    fun onBack()

    sealed interface Output {
        data object Back : Output
    }
}
