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

/**
 * Pure, Compose-free rendering decision for [IncomeScreen][com.inclinic.app.features.doctor.profile.presentation.ui.IncomeScreen].
 *
 * Previously the screen rendered nothing at all whenever `summary == null && error == null`
 * (the initial/still-loading instant, or a successful-but-empty backend response) — this
 * closes that gap by giving that combination its own [Empty] branch, distinct from [Error].
 */
sealed interface IncomeViewState {
    data object Loading : IncomeViewState
    data class Error(val message: String) : IncomeViewState
    data object Empty : IncomeViewState
    data class Content(val summary: IncomeSummary) : IncomeViewState
}

fun IncomeState.toViewState(): IncomeViewState = when {
    isLoading -> IncomeViewState.Loading
    // A loaded summary always wins over a stale/refresh error — mirrors
    // DetailLoadState's value-before-error precedence, so a background refresh
    // failure doesn't blank data that already rendered successfully.
    summary != null -> IncomeViewState.Content(summary)
    error != null -> IncomeViewState.Error(error)
    else -> IncomeViewState.Empty
}
