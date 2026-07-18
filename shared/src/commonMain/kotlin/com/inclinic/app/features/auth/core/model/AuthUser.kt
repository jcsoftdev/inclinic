package com.inclinic.app.features.auth.core.model

/**
 * Authenticated user entity.
 *
 * Note: @Immutable (Compose runtime) is intentionally omitted — the shared module does NOT
 * depend on compose-runtime in commonMain, and adding it would introduce an unnecessary
 * transitive Compose dependency to pure business logic. The data class is effectively
 * immutable by design (all properties are val).
 */
data class AuthUser(
    val id: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val role: UserRole,
    val doctorId: String? = null,
    val patientId: String? = null,
    /** Populated by GET /api/users/me; absent from the login/register response shape. */
    val phone: String? = null,
)
