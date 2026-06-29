package com.inclinic.app.features.doctor.prescriptions.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.doctor.prescriptions.core.model.Prescription
import com.inclinic.app.features.doctor.prescriptions.core.port.DoctorPrescriptionsRepository

class GetPrescriptionUseCase(
    private val repository: DoctorPrescriptionsRepository,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(id: String): Result<Prescription> = repository.getPrescription(id)
}
