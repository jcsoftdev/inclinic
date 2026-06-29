package com.inclinic.app.features.doctor.profile.presentation.component

import com.arkivanov.decompose.value.Value

interface ChangePasswordComponent {
    val state: Value<ChangePasswordState>

    fun onCurrentPasswordChange(value: String)
    fun onNewPasswordChange(value: String)
    fun onConfirmNewPasswordChange(value: String)
    fun onSubmit()
    fun onBack()

    sealed interface Output {
        data object Back : Output
    }
}

data class ChangePasswordState(
    val currentPassword: String = "",
    val newPassword: String = "",
    val confirmNewPassword: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false,
)
