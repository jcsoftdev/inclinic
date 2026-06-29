package com.inclinic.app.features.doctor.infrastructure.remote

import kotlinx.serialization.Serializable

@Serializable
data class DoctorPriceConfig(
    val id: String,
    val consultationFee: Double,
    val supportsPresential: Boolean,
    val supportsVirtual: Boolean,
)

interface DoctorProfileDataSource {
    suspend fun getPriceConfig(doctorId: String): Result<DoctorPriceConfig>
    suspend fun updatePriceConfig(doctorId: String, fee: Double, supportsPresential: Boolean, supportsVirtual: Boolean): Result<DoctorPriceConfig>
}
