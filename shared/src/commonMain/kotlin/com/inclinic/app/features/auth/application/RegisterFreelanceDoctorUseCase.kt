package com.inclinic.app.features.auth.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.auth.core.error.AuthError
import com.inclinic.app.features.auth.infrastructure.remote.AuthRemoteDataSource
import com.inclinic.app.features.auth.infrastructure.remote.dto.DoctorFreelanceRequestDto
import com.inclinic.app.features.auth.infrastructure.remote.dto.FreelanceScheduleDto
import kotlinx.coroutines.withContext

/**
 * Registers a doctor as a freelance provider via the PUBLIC endpoint
 * POST /api/doctors/freelance.
 *
 * No password is set at registration time — the backend sends an activation link
 * so the doctor sets their password later.
 *
 * Validation mirrors the backend [createFreelanceDoctorSchema]:
 * - firstName/lastName >= 2 chars
 * - valid email
 * - phone >= 6 chars
 * - at least one document URL
 * - at least one specialtyId
 * - consultationPrice >= 50
 * - at least one schedule
 */
class RegisterFreelanceDoctorUseCase(
    private val remote: AuthRemoteDataSource,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(params: Params): Result<Unit> = withContext(dispatchers.io) {
        val error = validate(params)
        if (error != null) return@withContext Result.failure(error)

        remote.registerFreelanceDoctor(params.toDto())
    }

    private fun validate(p: Params): AuthError? {
        if (p.firstName.trim().length < 2) return AuthError.ValidationError(
            field = AuthError.ValidationError.Field.NAME,
            kind = AuthError.ValidationError.Kind.EMPTY_NAME,
        )
        if (p.lastName.trim().length < 2) return AuthError.ValidationError(
            field = AuthError.ValidationError.Field.LAST_NAME,
            kind = AuthError.ValidationError.Kind.EMPTY_LAST_NAME,
        )
        if (!EMAIL_REGEX.matches(p.email)) return AuthError.ValidationError(
            field = AuthError.ValidationError.Field.EMAIL,
            kind = AuthError.ValidationError.Kind.INVALID_EMAIL,
        )
        if (p.phone.trim().length < 6) return AuthError.FreelanceValidationError(
            AuthError.FreelanceValidationError.Field.PHONE,
            "Teléfono debe tener al menos 6 caracteres",
        )
        if (p.documents.isEmpty()) return AuthError.FreelanceValidationError(
            AuthError.FreelanceValidationError.Field.DOCUMENTS,
            "Debe subir al menos un documento",
        )
        if (p.specialtyIds.isEmpty()) return AuthError.FreelanceValidationError(
            AuthError.FreelanceValidationError.Field.SPECIALTY_IDS,
            "Debe seleccionar al menos una especialidad",
        )
        if (p.consultationPrice < 50.0) return AuthError.FreelanceValidationError(
            AuthError.FreelanceValidationError.Field.CONSULTATION_PRICE,
            "El precio mínimo es S/. 50",
        )
        if (p.schedules.isEmpty()) return AuthError.FreelanceValidationError(
            AuthError.FreelanceValidationError.Field.SCHEDULES,
            "Debe agregar al menos un horario",
        )
        return null
    }

    private fun Params.toDto() = DoctorFreelanceRequestDto(
        firstName = firstName.trim(),
        lastName = lastName.trim(),
        email = email.trim(),
        phone = phone.trim(),
        licenseNumber = licenseNumber?.takeIf { it.isNotBlank() },
        bio = bio?.takeIf { it.isNotBlank() },
        documents = documents,
        appointmentMode = appointmentMode,
        appointmentDuration = appointmentDuration,
        specialtyIds = specialtyIds,
        primarySpecialtyId = primarySpecialtyId,
        consultationPrice = consultationPrice,
        offersHomeVisit = offersHomeVisit,
        serviceArea = serviceArea?.takeIf { it.isNotBlank() },
        schedules = schedules,
    )

    data class Params(
        val firstName: String,
        val lastName: String,
        val email: String,
        val phone: String,
        val licenseNumber: String? = null,
        val bio: String? = null,
        val documents: List<String> = emptyList(),
        val appointmentMode: String = "BY_SCHEDULE",
        val appointmentDuration: Int = 30,
        val specialtyIds: List<String> = emptyList(),
        val primarySpecialtyId: String = "",
        val consultationPrice: Double = 0.0,
        val offersHomeVisit: Boolean = false,
        val serviceArea: String? = null,
        val schedules: List<FreelanceScheduleDto> = emptyList(),
    )

    private companion object {
        val EMAIL_REGEX = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")
    }
}
