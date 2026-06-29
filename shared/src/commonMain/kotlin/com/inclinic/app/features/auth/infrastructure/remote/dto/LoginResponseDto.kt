package com.inclinic.app.features.auth.infrastructure.remote.dto

import kotlinx.serialization.Serializable

/**
 * Raw response from POST /api/auth/login.
 *
 * The backend may return either:
 *  (a) Normal login:   { user, accessToken, refreshToken }
 *  (b) 2FA required:  { requires2FA: true, partialToken }
 *
 * All fields are optional so both shapes deserialise into a single DTO without
 * a discriminated union on the wire. Domain mapping happens in [DefaultAuthRepository].
 */
@Serializable
data class LoginResponseDto(
    val user: UserDto? = null,
    val accessToken: String? = null,
    val refreshToken: String? = null,
    val requires2FA: Boolean = false,
    val partialToken: String? = null,
)

/**
 * Raw response from POST /api/auth/2fa/verify.
 * Always returns full tokens + user on success.
 */
@Serializable
data class TwoFactorVerifyResponseDto(
    val user: UserDto,
    val accessToken: String,
    val refreshToken: String,
)

/** Request body for POST /api/auth/2fa/verify. */
@Serializable
data class TwoFactorVerifyRequestDto(
    val partialToken: String,
    val code: String,
)
