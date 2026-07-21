package com.inclinic.app.features.admin.presentation.component

import kotlin.test.Test
import kotlin.test.assertEquals

class AdminFinanceViewStateTest {

    @Test
    fun still_loading_with_zero_balance_is_Loading() {
        assertEquals(
            AdminFinanceViewState.Loading,
            AdminFinanceState(isLoading = true, balanceTotal = "S/ 0").toViewState(),
        )
    }

    @Test
    fun failed_load_with_zero_balance_is_a_distinct_Failed_state() {
        assertEquals(
            AdminFinanceViewState.Failed("No se pudo conectar al servidor."),
            AdminFinanceState(
                isLoading = false,
                error = "No se pudo conectar al servidor.",
                balanceTotal = "S/ 0",
            ).toViewState(),
        )
    }

    @Test
    fun a_background_refresh_failure_after_a_real_balance_loaded_stays_Loaded() {
        assertEquals(
            AdminFinanceViewState.Loaded,
            AdminFinanceState(
                isLoading = false,
                error = "Fallo el refresco",
                balanceTotal = "S/ 1200",
                hasLoadedOnce = true,
            ).toViewState(),
        )
    }

    @Test
    fun a_loaded_then_error_state_with_a_genuinely_zero_balance_renders_Loaded_not_Failed() {
        // Regression for the old `balanceTotal != "S/ 0"` sentinel: a legitimately-zero
        // successful load followed by a refresh error must keep showing the loaded hero
        // card (with its dismissible error banner), not flip to the full Failed screen.
        assertEquals(
            AdminFinanceViewState.Loaded,
            AdminFinanceState(
                isLoading = false,
                error = "Fallo el refresco",
                balanceTotal = "S/ 0",
                hasLoadedOnce = true,
            ).toViewState(),
        )
    }

    @Test
    fun successful_load_is_Loaded() {
        assertEquals(
            AdminFinanceViewState.Loaded,
            AdminFinanceState(isLoading = false, error = null, balanceTotal = "S/ 500").toViewState(),
        )
    }

    @Test
    fun a_genuinely_zero_balance_with_no_loading_and_no_error_is_Loaded_not_stuck() {
        assertEquals(
            AdminFinanceViewState.Loaded,
            AdminFinanceState(isLoading = false, error = null, balanceTotal = "S/ 0").toViewState(),
        )
    }
}
