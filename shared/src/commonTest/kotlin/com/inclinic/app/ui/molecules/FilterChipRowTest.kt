package com.inclinic.app.ui.molecules

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * RED → GREEN tests for [isChipSelected].
 *
 * Pure helper extracted from FilterChipRow to validate selection logic without
 * a Compose runtime.
 */
class FilterChipRowTest {

    @Test
    fun selected_option_returns_true() {
        assertTrue(isChipSelected(option = "Todos", selected = "Todos"))
    }

    @Test
    fun non_selected_option_returns_false() {
        assertFalse(isChipSelected(option = "Activos", selected = "Todos"))
    }

    @Test
    fun empty_option_against_empty_selected_returns_true() {
        assertTrue(isChipSelected(option = "", selected = ""))
    }

    @Test
    fun empty_option_against_non_empty_selected_returns_false() {
        assertFalse(isChipSelected(option = "", selected = "Todos"))
    }

    @Test
    fun case_sensitive_mismatch_returns_false() {
        assertFalse(isChipSelected(option = "todos", selected = "Todos"))
    }

    @Test
    fun different_options_are_not_selected_when_another_is_selected() {
        val options = listOf("Todos", "Activos", "Inactivos")
        val selected = "Activos"
        val results = options.map { isChipSelected(it, selected) }
        assertTrue(results[1], "Activos should be selected")
        assertFalse(results[0], "Todos should not be selected")
        assertFalse(results[2], "Inactivos should not be selected")
    }
}
