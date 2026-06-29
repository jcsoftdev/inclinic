package com.inclinic.app.features.doctor.packages.fakes

import com.inclinic.app.features.doctor.packages.core.model.TherapyPackage
import com.inclinic.app.features.doctor.packages.core.port.DoctorPackagesRepository
import com.inclinic.app.features.doctor.packages.core.port.NewPackageDraft

class FakeDoctorPackagesRepository : DoctorPackagesRepository {

    var listResult: Result<List<TherapyPackage>> = Result.success(emptyList())
    var createResult: Result<TherapyPackage> = Result.success(samplePackage(id = "new-id"))
    var cancelResult: Result<Unit> = Result.success(Unit)

    var listCallCount = 0
    var createCallCount = 0
    var cancelCallCount = 0

    var lastCreated: NewPackageDraft? = null
    var lastCancelledId: String? = null

    override suspend fun list(): Result<List<TherapyPackage>> {
        listCallCount++
        return listResult
    }

    override suspend fun create(draft: NewPackageDraft): Result<TherapyPackage> {
        createCallCount++
        lastCreated = draft
        return createResult
    }

    override suspend fun cancel(id: String): Result<Unit> {
        cancelCallCount++
        lastCancelledId = id
        return cancelResult
    }
}
