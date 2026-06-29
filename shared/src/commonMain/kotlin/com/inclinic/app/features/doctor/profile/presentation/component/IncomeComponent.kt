package com.inclinic.app.features.doctor.profile.presentation.component

import com.arkivanov.decompose.value.Value
import com.inclinic.app.features.doctor.profile.core.model.IncomeSummary

interface IncomeComponent {
    val state: Value<IncomeState>

    fun onRetry()
    fun onBack()

    sealed interface Output {
        data object Back : Output
    }
}

data class IncomeState(
    val isLoading: Boolean = false,
    val summary: IncomeSummary? = null,
    val error: String? = null,
)
