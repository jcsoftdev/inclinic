package com.inclinic.app.features.auth.infrastructure.remote

import com.inclinic.app.features.auth.core.error.AuthError
import com.inclinic.app.features.auth.infrastructure.remote.dto.AuthErrorDto
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertEquals

/**
 * Unit tests for the pure error mapping function [mapHttpToAuthError].
 * Covers all 9 rows from the error map table in design doc §9.
 */
class AuthErrorMappingTest {

    // Row 1: 400 → InvalidCredentials
    @Test
    fun status_400_maps_to_invalid_credentials() {
        val error = mapHttpToAuthError(400, null)
        assertIs<AuthError.InvalidCredentials>(error)
    }

    // Row 2: 401, no code → InvalidCredentials
    @Test
    fun status_401_no_code_maps_to_invalid_credentials() {
        val error = mapHttpToAuthError(401, null)
        assertIs<AuthError.InvalidCredentials>(error)
    }

    // Row 2b: 401 with unknown code → InvalidCredentials
    @Test
    fun status_401_unknown_code_maps_to_invalid_credentials() {
        val error = mapHttpToAuthError(401, AuthErrorDto(error = "Unauthorized", code = "SOME_OTHER_CODE"))
        assertIs<AuthError.InvalidCredentials>(error)
    }

    // Row 3: 401 + code=INACTIVE → InactiveAccount
    @Test
    fun status_401_code_inactive_maps_to_inactive_account() {
        val error = mapHttpToAuthError(401, AuthErrorDto(error = "Unauthorized", code = "INACTIVE"))
        assertIs<AuthError.InactiveAccount>(error)
    }

    // Row 4: 403 + code=ACCOUNT_SUSPENDED → SuspendedAccount
    @Test
    fun status_403_code_account_suspended_maps_to_suspended_account() {
        val error = mapHttpToAuthError(403, AuthErrorDto(error = "Forbidden", code = "ACCOUNT_SUSPENDED"))
        assertIs<AuthError.SuspendedAccount>(error)
    }

    // Row 5: 500 → ServerError(500)
    @Test
    fun status_500_maps_to_server_error_with_correct_status() {
        val error = mapHttpToAuthError(500, null)
        assertIs<AuthError.ServerError>(error)
        assertEquals(500, error.status)
    }

    // Row 5b: 503 → ServerError(503)
    @Test
    fun status_503_maps_to_server_error_with_correct_status() {
        val error = mapHttpToAuthError(503, null)
        assertIs<AuthError.ServerError>(error)
        assertEquals(503, error.status)
    }

    // Row 6: 422 → InvalidCredentials
    @Test
    fun status_422_maps_to_invalid_credentials() {
        val error = mapHttpToAuthError(422, null)
        assertIs<AuthError.InvalidCredentials>(error)
    }

    // Row 7: anything else (e.g. 404) → Unknown
    @Test
    fun status_404_maps_to_unknown() {
        val error = mapHttpToAuthError(404, null)
        assertIs<AuthError.Unknown>(error)
    }

    // Row 7b: 302 (redirect) → Unknown
    @Test
    fun status_302_maps_to_unknown() {
        val error = mapHttpToAuthError(302, null)
        assertIs<AuthError.Unknown>(error)
    }

    // 403 with no code → Unknown (not suspended without the specific code)
    @Test
    fun status_403_no_code_maps_to_unknown() {
        val error = mapHttpToAuthError(403, null)
        assertIs<AuthError.Unknown>(error)
    }

    // Row 8: 429 (rate limited) → TooManyAttempts
    @Test
    fun status_429_maps_to_too_many_attempts() {
        val error = mapHttpToAuthError(429, null)
        assertIs<AuthError.TooManyAttempts>(error)
    }
}
