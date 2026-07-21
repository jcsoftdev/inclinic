package com.inclinic.app.core.util

import kotlin.test.Test
import kotlin.test.assertEquals

class DetailLoadStateTest {

    @Test
    fun loading_wins_even_with_stale_value_or_error() {
        assertEquals(
            DetailLoadState.Loading,
            detailLoadState(isLoading = true, value = null, error = null, notFound = false),
        )
        assertEquals(
            DetailLoadState.Loading,
            detailLoadState(isLoading = true, value = "stale", error = "stale error", notFound = true),
        )
    }

    @Test
    fun content_wins_over_a_lingering_error_once_a_value_is_present() {
        assertEquals(
            DetailLoadState.Content("doctor-1"),
            detailLoadState(isLoading = false, value = "doctor-1", error = "background refresh failed", notFound = false),
        )
    }

    @Test
    fun not_found_is_reported_when_flagged_and_no_value_loaded() {
        assertEquals(
            DetailLoadState.NotFound("No encontramos lo que buscabas."),
            detailLoadState(isLoading = false, value = null, error = "No encontramos lo que buscabas.", notFound = true),
        )
    }

    @Test
    fun not_found_falls_back_to_a_default_message_when_error_is_null() {
        assertEquals(
            DetailLoadState.NotFound("No encontrado."),
            detailLoadState(isLoading = false, value = null, error = null, notFound = true),
        )
    }

    @Test
    fun generic_failure_is_reported_when_not_flagged_as_not_found() {
        assertEquals(
            DetailLoadState.Failed("Error de red"),
            detailLoadState(isLoading = false, value = null, error = "Error de red", notFound = false),
        )
    }

    @Test
    fun defaults_to_loading_when_nothing_is_available_yet() {
        assertEquals(
            DetailLoadState.Loading,
            detailLoadState<String>(isLoading = false, value = null, error = null, notFound = false),
        )
    }
}
