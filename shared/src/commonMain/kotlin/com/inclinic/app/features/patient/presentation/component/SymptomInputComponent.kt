package com.inclinic.app.features.patient.presentation.component

import com.arkivanov.decompose.value.Value

interface SymptomInputComponent {
    val state: Value<SymptomInputState>
    fun onTextChanged(text: String)
    fun onChipToggle(chip: String)
    fun onSearch()
    fun onBack()
    sealed interface Output {
        data class NavigateToResults(val symptoms: String) : Output
        data object Back : Output
    }
}

data class SymptomInputState(
    val symptomText: String = "",
    val selectedChips: Set<String> = emptySet(),
    val isSearching: Boolean = false,
)
