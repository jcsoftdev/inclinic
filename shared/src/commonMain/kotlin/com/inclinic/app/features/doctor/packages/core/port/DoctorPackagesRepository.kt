package com.inclinic.app.features.doctor.packages.core.port

import com.inclinic.app.features.doctor.packages.core.model.TherapyPackage

/** Draft used to propose a new package to a patient. */
data class NewPackageDraft(
    val patientId: String,
    val specialtyId: String,
    val packageName: String,
    val totalSessions: Int,
    val regularPricePerSession: Double,
    val packagePricePerSession: Double,
    val isPrepaid: Boolean,
    val prepaidDiscount: Double?,
    val isHomeVisit: Boolean,
)

interface DoctorPackagesRepository {
    suspend fun list(): Result<List<TherapyPackage>>
    suspend fun create(draft: NewPackageDraft): Result<TherapyPackage>
    suspend fun cancel(id: String): Result<Unit>
}
