@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.inclinic.app.features.patient.presentation.component

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DefaultSymptomInputComponentTest {

    private val lifecycle = LifecycleRegistry().also { it.resume() }
    private val ctx = DefaultComponentContext(lifecycle = lifecycle)

    private fun createComponent(
        outputs: MutableList<SymptomInputComponent.Output> = mutableListOf(),
    ): DefaultSymptomInputComponent {
        return DefaultSymptomInputComponent(
            componentContext = ctx,
            onOutput = outputs::add,
        )
    }

    @Test
    fun initial_state_is_empty() = runTest {
        val component = createComponent()

        val state = component.state.value
        assertEquals("", state.symptomText)
        assertTrue(state.selectedChips.isEmpty())
        assertFalse(state.isSearching)
    }

    @Test
    fun onTextChanged_updates_symptomText() = runTest {
        val component = createComponent()

        component.onTextChanged("dolor de cabeza")

        assertEquals("dolor de cabeza", component.state.value.symptomText)
    }

    @Test
    fun onChipToggle_adds_chip_when_not_selected() = runTest {
        val component = createComponent()

        component.onChipToggle("fiebre")

        assertTrue(component.state.value.selectedChips.contains("fiebre"))
    }

    @Test
    fun onChipToggle_removes_chip_when_already_selected() = runTest {
        val component = createComponent()
        component.onChipToggle("fiebre")
        assertTrue(component.state.value.selectedChips.contains("fiebre"))

        component.onChipToggle("fiebre")

        assertFalse(component.state.value.selectedChips.contains("fiebre"))
    }

    @Test
    fun onChipToggle_can_select_multiple_chips() = runTest {
        val component = createComponent()

        component.onChipToggle("fiebre")
        component.onChipToggle("tos")

        val chips = component.state.value.selectedChips
        assertTrue(chips.contains("fiebre"))
        assertTrue(chips.contains("tos"))
        assertEquals(2, chips.size)
    }

    @Test
    fun onSearch_with_text_emits_NavigateToResults() = runTest {
        val outputs = mutableListOf<SymptomInputComponent.Output>()
        val component = createComponent(outputs = outputs)
        component.onTextChanged("dolor de cabeza")

        component.onSearch()

        assertEquals(1, outputs.size)
        val output = outputs.first()
        assertTrue(output is SymptomInputComponent.Output.NavigateToResults)
        assertEquals("dolor de cabeza", (output as SymptomInputComponent.Output.NavigateToResults).symptoms)
    }

    @Test
    fun onSearch_with_blank_text_does_not_emit() = runTest {
        val outputs = mutableListOf<SymptomInputComponent.Output>()
        val component = createComponent(outputs = outputs)

        component.onSearch()

        assertTrue(outputs.isEmpty())
    }

    @Test
    fun onSearch_trims_whitespace_before_emitting() = runTest {
        val outputs = mutableListOf<SymptomInputComponent.Output>()
        val component = createComponent(outputs = outputs)
        component.onTextChanged("  fiebre  ")

        component.onSearch()

        assertEquals(1, outputs.size)
        val output = outputs.first() as SymptomInputComponent.Output.NavigateToResults
        assertEquals("fiebre", output.symptoms)
    }

    @Test
    fun onBack_emits_Back_output() = runTest {
        val outputs = mutableListOf<SymptomInputComponent.Output>()
        val component = createComponent(outputs = outputs)

        component.onBack()

        assertEquals(1, outputs.size)
        assertTrue(outputs.first() is SymptomInputComponent.Output.Back)
    }
}
