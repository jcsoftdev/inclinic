package com.inclinic.app.features.auth.infrastructure.remote.dto

import kotlinx.serialization.Serializable

/**
 * Generic envelope for API responses that wrap the payload.
 * Used when the backend returns `{ "success": true, "data": {...} }`.
 */
@Serializable
data class ApiEnvelopeDto<T>(
    val success: Boolean,
    val data: T? = null,
    val error: String? = null,
    val code: String? = null,
)
