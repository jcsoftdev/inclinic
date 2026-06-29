package com.inclinic.app.features.doctor.config.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.doctor.infrastructure.remote.DoctorPriceConfig
import com.inclinic.app.features.doctor.infrastructure.remote.DoctorProfileDataSource
import kotlinx.coroutines.withContext

class GetDoctorPriceConfigUseCase(
    private val dataSource: DoctorProfileDataSource,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(doctorId: String): Result<DoctorPriceConfig> =
        withContext(dispatchers.io) { dataSource.getPriceConfig(doctorId) }
}
