package com.inclinic.app.features.doctor.profile.presentation

import com.inclinic.app.features.doctor.profile.core.model.IncomeSummary
import com.inclinic.app.features.doctor.profile.presentation.component.IncomeState
import com.inclinic.app.features.doctor.profile.presentation.component.IncomeViewState
import com.inclinic.app.features.doctor.profile.presentation.component.toViewState
import kotlin.test.Test
import kotlin.test.assertEquals

private fun fakeSummary(): IncomeSummary = IncomeSummary(
    totalCents = 18000L,
    netCents = 15300L,
    commissionCents = 2700L,
    availableCents = 15300L,
    sessions = 12,
    growthPct = 8.0,
    breakdown = null,
)

class IncomeViewStateTest {

    @Test
    fun loading_wins_regardless_of_summary_or_error() {
        assertEquals(IncomeViewState.Loading, IncomeState(isLoading = true).toViewState())
        assertEquals(
            IncomeViewState.Loading,
            IncomeState(isLoading = true, summary = fakeSummary(), error = "stale").toViewState(),
        )
    }

    @Test
    fun still_initial_or_loaded_but_empty_renders_the_Empty_branch_not_nothing() {
        assertEquals(IncomeViewState.Empty, IncomeState().toViewState())
    }

    @Test
    fun api_error_is_distinct_from_empty() {
        assertEquals(
            IncomeViewState.Error("No se pudo conectar al servidor."),
            IncomeState(error = "No se pudo conectar al servidor.").toViewState(),
        )
    }

    @Test
    fun summary_present_renders_Content() {
        val summary = fakeSummary()
        assertEquals(IncomeViewState.Content(summary), IncomeState(summary = summary).toViewState())
    }

    @Test
    fun refresh_error_after_summary_already_loaded_keeps_showing_Content_not_Error() {
        // A background refresh failure must not blank data that already rendered
        // successfully — mirrors DetailLoadState's value-before-error precedence.
        val summary = fakeSummary()
        assertEquals(
            IncomeViewState.Content(summary),
            IncomeState(summary = summary, error = "No se pudo conectar al servidor.").toViewState(),
        )
    }
}
