package com.inclinic.app.core.model

import kotlinx.serialization.Serializable

@Serializable
data class PatientProfile(
    val id: String,
    val name: String,
    val email: String,
    val phone: String?,
    val dateOfBirth: String?,
    val photoUrl: String?,
)
