package com.inclinic.app.core.network

import kotlinx.serialization.Serializable

@Serializable
data class ApiEnvelope<T>(
    val success: Boolean = true,
    val data: T? = null,
)

@Serializable
data class PagedApiEnvelope<T>(
    val success: Boolean = true,
    val data: T? = null,
    val hasMore: Boolean = false,
)

@Serializable
data class ApiErrorEnvelope(
    val error: String = "Unknown error",
    val code: String? = null,
)
