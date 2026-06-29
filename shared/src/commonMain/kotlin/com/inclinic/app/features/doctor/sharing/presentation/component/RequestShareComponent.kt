package com.inclinic.app.features.doctor.sharing.presentation.component

import com.arkivanov.decompose.value.Value

interface RequestShareComponent {
    val state: Value<RequestShareState>

    fun onPatientIdChange(value: String)
    fun onReasonChange(value: String)
    fun onSubmit()
    fun onBack()

    sealed interface Output {
        data object Success : Output
        data object Back : Output
    }
}

data class RequestShareState(
    val patientId: String = "",
    val reason: String = "",
    val isSubmitting: Boolean = false,
    val error: String? = null,
    val success: Boolean = false,
)
