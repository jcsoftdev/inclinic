package com.inclinic.app.features.doctor.config.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.doctor.infrastructure.remote.DoctorPriceConfig
import com.inclinic.app.features.doctor.infrastructure.remote.DoctorProfileDataSource
import kotlinx.coroutines.withContext

class UpdateDoctorPriceConfigUseCase(
    private val dataSource: DoctorProfileDataSource,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(
        doctorId: String,
        fee: Double,
        supportsPresential: Boolean,
        supportsVirtual: Boolean,
    ): Result<DoctorPriceConfig> = withContext(dispatchers.io) {
        if (fee <= 0 || fee > 9999.99) {
            return@withContext Result.failure(
                IllegalArgumentException("Price must be between S/.0.01 and S/.9999.99")
            )
        }
        if (!supportsPresential && !supportsVirtual) {
            return@withContext Result.failure(
                IllegalArgumentException("At least one visit modality must be enabled")
            )
        }
        dataSource.updatePriceConfig(doctorId, fee, supportsPresential, supportsVirtual)
    }
}
