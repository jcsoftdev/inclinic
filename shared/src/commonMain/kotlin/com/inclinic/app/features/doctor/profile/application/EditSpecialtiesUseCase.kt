package com.inclinic.app.features.doctor.profile.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.doctor.profile.core.port.DoctorProfileRepository
import kotlinx.coroutines.withContext

class EditSpecialtiesUseCase(
    private val repository: DoctorProfileRepository,
    private val dispatchers: AppDispatchers,
) {
    /**
     * Replace the doctor's active specialties with [specialtyIds].
     * An empty list is allowed (removes all specialties).
     */
    suspend operator fun invoke(specialtyIds: List<String>): Result<Unit> =
        withContext(dispatchers.io) { repository.editSpecialties(specialtyIds) }
}
