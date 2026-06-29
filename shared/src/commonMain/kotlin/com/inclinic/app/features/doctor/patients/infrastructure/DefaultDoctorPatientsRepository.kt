package com.inclinic.app.features.doctor.patients.infrastructure

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.doctor.patients.core.model.PatientList
import com.inclinic.app.features.doctor.patients.core.model.PatientListItem
import com.inclinic.app.features.doctor.patients.core.model.PatientListStats
import com.inclinic.app.features.doctor.patients.core.model.PatientStatus
import com.inclinic.app.features.doctor.patients.core.port.DoctorPatientsRepository
import com.inclinic.app.features.doctor.patients.infrastructure.remote.DoctorPatientsDataSource
import com.inclinic.app.features.doctor.patients.infrastructure.remote.dto.PatientListItemDto
import kotlinx.coroutines.withContext

class DefaultDoctorPatientsRepository(
    private val remote: DoctorPatientsDataSource,
    private val dispatchers: AppDispatchers,
) : DoctorPatientsRepository {

    override suspend fun getPatients(): Result<PatientList> =
        withContext(dispatchers.io) {
            remote.getPatients().map { dto ->
                PatientList(
                    items = dto.patients.map { it.toDomain() },
                    stats = PatientListStats(
                        total = dto.stats.total,
                        active = dto.stats.active,
                        premium = dto.stats.premium,
                    ),
                )
            }
        }

    override suspend fun searchPatientByEmail(query: String): Result<List<PatientListItem>> =
        withContext(dispatchers.io) {
            remote.searchPatientByEmail(query).map { dtos -> dtos.map { it.toDomain() } }
        }

    private fun PatientListItemDto.toDomain() =
        PatientListItem(
            id = id,
            name = name,
            lastVisitDate = lastVisitDate,
            avatarUrl = avatarUrl,
            totalAppointments = totalAppointments,
            status = when (status) {
                "premium" -> PatientStatus.PREMIUM
                "active" -> PatientStatus.ACTIVE
                "inactive" -> PatientStatus.INACTIVE
                else -> PatientStatus.UNKNOWN
            },
        )
}
