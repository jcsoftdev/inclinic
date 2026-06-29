package com.inclinic.app.features.doctor.sharing.fakes

import com.inclinic.app.features.doctor.sharing.infrastructure.remote.DoctorSharingDataSource
import com.inclinic.app.features.doctor.sharing.infrastructure.remote.dto.CreateShareRequestDto
import com.inclinic.app.features.doctor.sharing.infrastructure.remote.dto.PatientUserDto
import com.inclinic.app.features.doctor.sharing.infrastructure.remote.dto.PatientUserInfoDto
import com.inclinic.app.features.doctor.sharing.infrastructure.remote.dto.ShareRequestDto

class FakeDoctorSharingDataSource : DoctorSharingDataSource {

    var listResult: Result<List<ShareRequestDto>> = Result.success(emptyList())
    var requestResult: Result<ShareRequestDto> = Result.success(stubDto("req-1"))
    var cancelResult: Result<Unit> = Result.success(Unit)

    var cancelledId: String? = null

    override suspend fun listRequests() = listResult
    override suspend fun requestShare(body: CreateShareRequestDto) = requestResult
    override suspend fun cancelRequest(id: String): Result<Unit> {
        cancelledId = id
        return cancelResult
    }
}

fun stubDto(id: String, status: String = "PENDING") = ShareRequestDto(
    id = id,
    patientId = "patient-1",
    requesterDoctorId = "doc-2",
    reason = "Consulta de seguimiento médico del paciente",
    scope = "FULL_HISTORY",
    status = status,
    createdAt = "1970-01-01T00:00:00Z",
    expiresAt = "1970-01-08T00:00:00Z",
    patient = PatientUserDto(user = PatientUserInfoDto(firstName = "Juan", lastName = "Pérez", email = "juan@test.com")),
)
