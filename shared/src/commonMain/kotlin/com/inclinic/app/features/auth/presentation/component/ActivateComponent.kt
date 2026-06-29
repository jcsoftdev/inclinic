package com.inclinic.app.features.auth.presentation.component

import com.arkivanov.decompose.value.Value

interface ActivateComponent {
    val state: Value<ActivateState>
    val email: String

    fun onCodeChanged(code: String)
    fun onSubmit()
    fun onResend()
    fun onBack()

    sealed interface Output {
        data object Success : Output
        data object Back : Output
    }
}
