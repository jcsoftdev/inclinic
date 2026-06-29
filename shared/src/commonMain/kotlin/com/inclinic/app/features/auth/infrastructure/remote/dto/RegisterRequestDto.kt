package com.inclinic.app.features.auth.infrastructure.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequestDto(
    val name: String,
    val email: String,
    val password: String,
    val role: String,
    val specialtyId: String? = null,
)
