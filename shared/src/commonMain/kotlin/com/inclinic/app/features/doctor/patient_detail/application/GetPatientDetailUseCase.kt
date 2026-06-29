package com.inclinic.app.features.doctor.patient_detail.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.core.model.Appointment
import com.inclinic.app.features.doctor.infrastructure.remote.DoctorPatientDataSource
import com.inclinic.app.features.doctor.infrastructure.remote.PatientDetail
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

data class PatientDetailWithHistory(
    val patient: PatientDetail,
    val recentAppointments: List<Appointment>,
)

class GetPatientDetailUseCase(
    private val dataSource: DoctorPatientDataSource,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(patientId: String): Result<PatientDetailWithHistory> =
        withContext(dispatchers.io) {
            val patientDeferred = async { dataSource.getPatientDetail(patientId) }
            val appointmentsDeferred = async { dataSource.getPatientAppointments(patientId, limit = 10) }
            val patient = patientDeferred.await().getOrElse { return@withContext Result.failure(it) }
            val appointments = appointmentsDeferred.await().getOrElse { emptyList() }
            Result.success(PatientDetailWithHistory(patient = patient, recentAppointments = appointments))
        }
}
