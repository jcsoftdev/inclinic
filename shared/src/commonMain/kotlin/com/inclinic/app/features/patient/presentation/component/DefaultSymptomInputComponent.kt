package com.inclinic.app.features.patient.presentation.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update

class DefaultSymptomInputComponent(
    componentContext: ComponentContext,
    private val onOutput: (SymptomInputComponent.Output) -> Unit,
) : SymptomInputComponent, ComponentContext by componentContext {

    private val _state = MutableValue(SymptomInputState())
    override val state: Value<SymptomInputState> = _state

    override fun onTextChanged(text: String) { _state.update { it.copy(symptomText = text) } }

    override fun onChipToggle(chip: String) {
        _state.update { current ->
            val chips = current.selectedChips.toMutableSet()
            if (chips.contains(chip)) chips.remove(chip) else chips.add(chip)
            current.copy(selectedChips = chips)
        }
    }

    override fun onSearch() {
        val text = _state.value.symptomText.trim()
        if (text.isBlank()) return
        onOutput(SymptomInputComponent.Output.NavigateToResults(text))
    }

    override fun onBack() {
        onOutput(SymptomInputComponent.Output.Back)
    }
}
