package com.inclinic.app.features.auth.infrastructure.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val id: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val role: String,
    val doctorId: String? = null,
    val patientId: String? = null,
    val phone: String? = null,
)
