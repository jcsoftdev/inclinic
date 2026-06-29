package com.inclinic.app.features.doctor.packages.infrastructure

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.doctor.packages.core.model.PackageSession
import com.inclinic.app.features.doctor.packages.core.model.PackageSessionStatus
import com.inclinic.app.features.doctor.packages.core.model.PackageStatus
import com.inclinic.app.features.doctor.packages.core.model.TherapyPackage
import com.inclinic.app.features.doctor.packages.core.port.DoctorPackagesRepository
import com.inclinic.app.features.doctor.packages.core.port.NewPackageDraft
import com.inclinic.app.features.doctor.packages.infrastructure.remote.DoctorPackagesDataSource
import com.inclinic.app.features.doctor.packages.infrastructure.remote.dto.CreatePackageRequestDto
import com.inclinic.app.features.doctor.packages.infrastructure.remote.dto.PackageSessionDto
import com.inclinic.app.features.doctor.packages.infrastructure.remote.dto.TherapyPackageDto
import kotlinx.coroutines.withContext

class DefaultDoctorPackagesRepository(
    private val remote: DoctorPackagesDataSource,
    private val dispatchers: AppDispatchers,
) : DoctorPackagesRepository {

    override suspend fun list(): Result<List<TherapyPackage>> =
        withContext(dispatchers.io) {
            remote.list().map { dtos -> dtos.map { it.toDomain() } }
        }

    override suspend fun create(draft: NewPackageDraft): Result<TherapyPackage> =
        withContext(dispatchers.io) {
            remote.create(draft.toCreateDto()).map { it.toDomain() }
        }

    override suspend fun cancel(id: String): Result<Unit> =
        withContext(dispatchers.io) { remote.cancel(id) }

    private fun TherapyPackageDto.toDomain(): TherapyPackage {
        val firstName = patient?.user?.firstName.orEmpty()
        val lastName = patient?.user?.lastName.orEmpty()
        return TherapyPackage(
            id = id,
            patientId = patientId,
            patientName = listOf(firstName, lastName).filter { it.isNotBlank() }.joinToString(" "),
            patientEmail = patient?.user?.email.orEmpty(),
            specialtyId = specialtyId,
            specialtyName = specialty?.name.orEmpty(),
            packageName = packageName,
            totalSessions = totalSessions,
            regularPricePerSession = regularPricePerSession,
            packagePricePerSession = packagePricePerSession,
            isPrepaid = isPrepaid,
            prepaidDiscount = prepaidDiscount,
            totalPrepaidAmount = totalPrepaidAmount,
            sessionsCompleted = sessionsCompleted,
            sessionsScheduled = sessionsScheduled,
            sessionsUsed = sessionsUsed,
            status = PackageStatus.fromRaw(status),
            sessions = sessions.mapIndexed { idx, dto -> dto.toDomain(idx, totalSessions) },
        )
    }

    private fun PackageSessionDto.toDomain(index: Int, total: Int): PackageSession {
        val number = sessionNumber ?: (index + 1)
        val sessionStatus = when (status?.uppercase()) {
            "COMPLETED", "USED" -> PackageSessionStatus.COMPLETED
            "SCHEDULED", "CONFIRMED", "PENDING", "UPCOMING" -> PackageSessionStatus.UPCOMING
            else -> if (startTime.isNullOrBlank()) PackageSessionStatus.UNSCHEDULED else PackageSessionStatus.UPCOMING
        }
        val subtitle = when (sessionStatus) {
            PackageSessionStatus.COMPLETED -> "Completada"
            PackageSessionStatus.UPCOMING -> startTime ?: "Próxima sesión"
            PackageSessionStatus.UNSCHEDULED -> "Programar próxima fecha"
        }
        return PackageSession(
            id = id,
            index = number,
            title = "Sesión $number",
            subtitle = subtitle,
            status = sessionStatus,
        )
    }

    private fun NewPackageDraft.toCreateDto() = CreatePackageRequestDto(
        patientId = patientId,
        specialtyId = specialtyId,
        packageName = packageName,
        totalSessions = totalSessions,
        regularPricePerSession = regularPricePerSession,
        packagePricePerSession = packagePricePerSession,
        isPrepaid = isPrepaid,
        prepaidDiscount = prepaidDiscount,
        isHomeVisit = isHomeVisit,
    )
}
