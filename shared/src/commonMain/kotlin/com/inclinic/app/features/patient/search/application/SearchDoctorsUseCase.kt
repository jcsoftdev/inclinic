package com.inclinic.app.features.patient.search.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.patient.infrastructure.remote.DoctorFilters
import com.inclinic.app.features.patient.infrastructure.remote.DoctorSearchDataSource
import com.inclinic.app.features.patient.infrastructure.remote.PagedDoctors
import kotlinx.coroutines.withContext

class SearchDoctorsUseCase(
    private val dataSource: DoctorSearchDataSource,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(
        filters: DoctorFilters,
        page: Int,
    ): Result<PagedDoctors> = withContext(dispatchers.io) {
        dataSource.searchDoctors(filters, page)
    }
}
