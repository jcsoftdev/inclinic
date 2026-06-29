package com.inclinic.app.ui.molecules

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * RED → GREEN tests for [normalizeBars].
 *
 * Pure helper that scales a list of [IncomeBar] amounts into 0..1 Float values
 * suitable for rendering bar heights.
 */
class IncomeChartTest {

    @Test
    fun empty_list_returns_empty() {
        val result = normalizeBars(emptyList())
        assertEquals(emptyList(), result)
    }

    @Test
    fun single_bar_returns_one() {
        val result = normalizeBars(listOf(IncomeBar("Ene", 500.0)))
        assertEquals(listOf(1f), result)
    }

    @Test
    fun max_bar_normalizes_to_one() {
        val bars = listOf(
            IncomeBar("Ene", 100.0),
            IncomeBar("Feb", 500.0),
            IncomeBar("Mar", 250.0),
        )
        val result = normalizeBars(bars)
        assertEquals(1f, result[1], "Max bar must normalize to 1f")
    }

    @Test
    fun zero_bar_normalizes_to_zero() {
        val bars = listOf(
            IncomeBar("Ene", 0.0),
            IncomeBar("Feb", 400.0),
        )
        val result = normalizeBars(bars)
        assertEquals(0f, result[0], "Zero amount must normalize to 0f")
    }

    @Test
    fun all_equal_bars_normalize_to_one() {
        val bars = listOf(
            IncomeBar("Ene", 300.0),
            IncomeBar("Feb", 300.0),
            IncomeBar("Mar", 300.0),
        )
        val result = normalizeBars(bars)
        assertTrue(result.all { it == 1f }, "All equal bars should be 1f: $result")
    }

    @Test
    fun normalized_values_are_in_zero_to_one_range() {
        val bars = listOf(
            IncomeBar("Ene", 100.0),
            IncomeBar("Feb", 200.0),
            IncomeBar("Mar", 150.0),
            IncomeBar("Abr", 400.0),
        )
        val result = normalizeBars(bars)
        assertTrue(result.all { it in 0f..1f }, "All values must be in [0,1]: $result")
    }

    @Test
    fun result_size_matches_input_size() {
        val bars = listOf(
            IncomeBar("Ene", 100.0),
            IncomeBar("Feb", 200.0),
            IncomeBar("Mar", 150.0),
        )
        assertEquals(bars.size, normalizeBars(bars).size)
    }

    @Test
    fun proportions_are_correct() {
        val bars = listOf(
            IncomeBar("A", 200.0),
            IncomeBar("B", 400.0),
        )
        val result = normalizeBars(bars)
        assertEquals(0.5f, result[0], "200/400 should normalize to 0.5f")
        assertEquals(1.0f, result[1])
    }
}
