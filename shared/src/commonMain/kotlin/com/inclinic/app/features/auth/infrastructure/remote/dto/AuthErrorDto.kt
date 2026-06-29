package com.inclinic.app.features.auth.infrastructure.remote.dto

import kotlinx.serialization.Serializable

/**
 * Error body shape returned by the backend on 4xx/5xx responses.
 * [code] is optional — its presence drives the fine-grained error mapping
 * (e.g. INACTIVE → InactiveAccount, ACCOUNT_SUSPENDED → SuspendedAccount).
 */
@Serializable
data class AuthErrorDto(
    val error: String,
    val code: String? = null,
)
