package com.inclinic.app.features.admin.presentation.component

import kotlin.test.Test
import kotlin.test.assertEquals

class AdminDashboardViewStateTest {

    @Test
    fun still_loading_with_zeroed_KPIs_is_Loading() {
        assertEquals(
            AdminDashboardViewState.Loading,
            AdminDashboardState(isLoading = true, appointmentsToday = 0, pendingDoctors = 0).toViewState(),
        )
    }

    @Test
    fun failed_load_with_zeroed_KPIs_is_a_distinct_Failed_state_not_the_loaded_grid() {
        assertEquals(
            AdminDashboardViewState.Failed("No se pudo conectar al servidor."),
            AdminDashboardState(
                isLoading = false,
                error = "No se pudo conectar al servidor.",
                appointmentsToday = 0,
                pendingDoctors = 0,
            ).toViewState(),
        )
    }

    @Test
    fun a_background_refresh_failure_after_data_already_loaded_stays_Loaded() {
        assertEquals(
            AdminDashboardViewState.Loaded,
            AdminDashboardState(
                isLoading = false,
                error = "Fallo el refresco",
                appointmentsToday = 5,
                pendingDoctors = 0,
                hasLoadedOnce = true,
            ).toViewState(),
        )
    }

    @Test
    fun a_loaded_then_error_state_with_genuinely_zero_KPIs_renders_Loaded_not_Failed() {
        // Regression for the old `appointmentsToday != 0 || pendingDoctors != 0` sentinel:
        // a legitimately-zero successful load followed by a refresh error must keep showing
        // the loaded grid (with its dismissible error banner), not flip to the full Failed screen.
        assertEquals(
            AdminDashboardViewState.Loaded,
            AdminDashboardState(
                isLoading = false,
                error = "Fallo el refresco",
                appointmentsToday = 0,
                pendingDoctors = 0,
                hasLoadedOnce = true,
            ).toViewState(),
        )
    }

    @Test
    fun successful_load_is_Loaded() {
        assertEquals(
            AdminDashboardViewState.Loaded,
            AdminDashboardState(isLoading = false, error = null, appointmentsToday = 3, pendingDoctors = 2).toViewState(),
        )
    }

    @Test
    fun zero_KPIs_with_no_loading_and_no_error_is_Loaded_not_stuck() {
        // A legitimately empty dashboard (0 appointments, 0 pending doctors, no error) must
        // still render the loaded grid, not get stuck in a Loading/Failed limbo.
        assertEquals(
            AdminDashboardViewState.Loaded,
            AdminDashboardState(isLoading = false, error = null, appointmentsToday = 0, pendingDoctors = 0).toViewState(),
        )
    }
}
