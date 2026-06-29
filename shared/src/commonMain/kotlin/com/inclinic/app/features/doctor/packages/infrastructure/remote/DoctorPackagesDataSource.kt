package com.inclinic.app.features.doctor.packages.infrastructure.remote

import com.inclinic.app.features.doctor.packages.infrastructure.remote.dto.CreatePackageRequestDto
import com.inclinic.app.features.doctor.packages.infrastructure.remote.dto.TherapyPackageDto

interface DoctorPackagesDataSource {
    suspend fun list(): Result<List<TherapyPackageDto>>
    suspend fun create(request: CreatePackageRequestDto): Result<TherapyPackageDto>
    suspend fun cancel(id: String): Result<Unit>
}
