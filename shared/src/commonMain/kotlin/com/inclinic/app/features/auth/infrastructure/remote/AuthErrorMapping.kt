package com.inclinic.app.features.auth.infrastructure.remote

import com.inclinic.app.features.auth.core.error.AuthError
import com.inclinic.app.features.auth.infrastructure.remote.dto.AuthErrorDto

/**
 * Pure function: maps HTTP status code + optional error body to the correct [AuthError] subclass.
 *
 * Error table (from design doc §9):
 * | Status | Body code            | AuthError             |
 * |--------|----------------------|-----------------------|
 * | 400    | –                    | InvalidCredentials    |
 * | 422    | –                    | InvalidCredentials    |
 * | 401    | (no code / unknown)  | InvalidCredentials    |
 * | 401    | "INACTIVE"           | InactiveAccount       |
 * | 403    | "ACCOUNT_SUSPENDED"  | SuspendedAccount      |
 * | 429    | –                    | TooManyAttempts       |
 * | 5xx    | –                    | ServerError(status)   |
 * | other  | –                    | Unknown               |
 *
 * Note: 403 without "ACCOUNT_SUSPENDED" code → Unknown (not a defined auth domain error).
 */
fun mapHttpToAuthError(status: Int, body: AuthErrorDto?): AuthError = when {
    status == 400 || status == 422 -> AuthError.InvalidCredentials

    status == 401 -> when (body?.code) {
        "INACTIVE" -> AuthError.InactiveAccount
        "INVALID_TOKEN", "EXPIRED_TOKEN" -> AuthError.InvalidToken
        else -> AuthError.InvalidCredentials
    }

    status == 403 && body?.code == "ACCOUNT_SUSPENDED" -> AuthError.SuspendedAccount

    status == 409 -> AuthError.EmailAlreadyExists

    status == 429 -> AuthError.TooManyAttempts

    status in 500..599 -> AuthError.ServerError(status)

    else -> AuthError.Unknown(IllegalStateException("Unexpected HTTP $status"))
}
