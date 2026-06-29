package com.inclinic.app.features.doctor.onboarding.presentation.component

import com.arkivanov.decompose.value.Value

data class CorregirSolicitudState(
    val corrections: Map<String, String> = emptyMap(),
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val submitSuccess: Boolean = false,
    val error: String? = null,
)

interface CorregirSolicitudComponent {
    val state: Value<CorregirSolicitudState>

    fun onFieldChanged(field: String, value: String)
    fun onSubmitClicked()
    fun onErrorDismissed()

    sealed interface Output {
        data object LogOut : Output
    }
}
