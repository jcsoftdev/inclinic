package com.inclinic.app.features.doctor.patients.core.model

enum class PatientStatus { PREMIUM, ACTIVE, INACTIVE, UNKNOWN }

data class PatientListItem(
    val id: String,
    val name: String,
    val lastVisitDate: String?,
    val avatarUrl: String?,
    val totalAppointments: Int,
    val status: PatientStatus = PatientStatus.UNKNOWN,
)

data class PatientListStats(
    val total: Int = 0,
    val active: Int = 0,
    val premium: Int = 0,
)

data class PatientList(
    val items: List<PatientListItem> = emptyList(),
    val stats: PatientListStats = PatientListStats(),
)
