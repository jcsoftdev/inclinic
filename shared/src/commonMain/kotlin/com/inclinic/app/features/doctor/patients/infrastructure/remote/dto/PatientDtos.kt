package com.inclinic.app.features.doctor.patients.infrastructure.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class PatientListItemDto(
    val id: String,
    val name: String,
    val lastVisitDate: String? = null,
    val avatarUrl: String? = null,
    val totalAppointments: Int = 0,
    /** "premium" | "active" | "inactive" — resuelto por el backend. */
    val status: String? = null,
)

@Serializable
data class PatientListStatsDto(
    val total: Int = 0,
    val active: Int = 0,
    val premium: Int = 0,
)

@Serializable
data class PatientListDto(
    val patients: List<PatientListItemDto> = emptyList(),
    val stats: PatientListStatsDto = PatientListStatsDto(),
)

/**
 * Respuesta cruda de `GET /api/patients/search`: un único paciente con el
 * usuario embebido (shape de Prisma). El doctor busca por email para invitar
 * o crear paquetes.
 */
@Serializable
data class PatientSearchDto(
    val id: String,
    val avatar: String? = null,
    val user: PatientSearchUserDto,
)

@Serializable
data class PatientSearchUserDto(
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
)
