package com.inclinic.app.features.patient.doctor_profile.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.core.model.Review
import com.inclinic.app.features.patient.infrastructure.remote.DoctorSearchDataSource
import kotlinx.coroutines.withContext

class GetDoctorReviewsUseCase(
    private val dataSource: DoctorSearchDataSource,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(doctorId: String, page: Int): Result<List<Review>> =
        withContext(dispatchers.io) { dataSource.getDoctorReviews(doctorId, page) }
}
