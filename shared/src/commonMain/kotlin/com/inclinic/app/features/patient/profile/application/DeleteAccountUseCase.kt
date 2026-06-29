package com.inclinic.app.features.patient.profile.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.patient.infrastructure.remote.PatientDataSource
import kotlinx.coroutines.withContext

/**
 * Eliminación de cuenta del paciente confirmada con contraseña (Ley 29733).
 * Llama a POST /api/users/me/delete. La limpieza de sesión y la navegación a Auth
 * las maneja el componente (LogoutUseCase + SessionEvents), no este caso de uso.
 */
class DeleteAccountUseCase(
    private val dataSource: PatientDataSource,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(password: String, reason: String? = null): Result<Unit> =
        withContext(dispatchers.io) { dataSource.deleteAccount(password, reason) }
}
