package com.inclinic.app.features.auth.presentation.component

import com.arkivanov.decompose.value.Value

interface RegisterPatientComponent {
    val state: Value<RegisterPatientState>

    fun onNameChanged(name: String)
    fun onLastNameChanged(lastName: String)
    fun onEmailChanged(email: String)
    fun onPhoneChanged(phone: String)
    fun onPasswordChanged(password: String)
    fun onConfirmPasswordChanged(confirmPassword: String)
    fun onSubmit()
    fun onBack()

    sealed interface Output {
        data class Success(val email: String) : Output
        data object Back : Output
    }
}
