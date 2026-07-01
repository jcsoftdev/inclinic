package com.inclinic.app.features.patient.fakes

import com.inclinic.app.core.model.MedicalProfile
import com.inclinic.app.core.model.PatientProfile
import com.inclinic.app.features.patient.infrastructure.remote.PatientDashboard
import com.inclinic.app.features.patient.infrastructure.remote.PatientDataSource

/**
 * In-memory fake for [PatientDataSource].
 * Tests configure results before calling; call counts verify no-network guarantees.
 */
class FakePatientDataSource : PatientDataSource {

    companion object {
        val defaultProfile = PatientProfile(
            id = "pat-1",
            name = "Ana Torres",
            email = "ana@inclinic.com",
            phone = null,
            dateOfBirth = null,
            photoUrl = null,
        )

        val defaultDashboard = PatientDashboard(
            upcomingCount = 0,
            recentDoctors = emptyList(),
            nextAppointment = null,
        )
    }

    var getPatientProfileResult: Result<PatientProfile> = Result.success(defaultProfile)
    var updatePatientProfileResult: Result<PatientProfile> = Result.success(defaultProfile)
    var getMedicalProfileResult: Result<MedicalProfile> = Result.success(MedicalProfile.empty())
    var updateMedicalProfileResult: Result<MedicalProfile> = Result.success(MedicalProfile.empty())
    var getDashboardResult: Result<PatientDashboard> = Result.success(defaultDashboard)
    var deleteAccountResult: Result<Unit> = Result.success(Unit)
    var changePasswordResult: Result<Unit> = Result.success(Unit)

    var getPatientProfileCallCount = 0
    var updatePatientProfileCallCount = 0
    var getMedicalProfileCallCount = 0
    var updateMedicalProfileCallCount = 0
    var getDashboardCallCount = 0
    var deleteAccountCallCount = 0
    var changePasswordCallCount = 0

    var lastChangePasswordCurrent: String? = null
    var lastChangePasswordNew: String? = null

    override suspend fun getPatientProfile(patientId: String): Result<PatientProfile> {
        getPatientProfileCallCount++
        return getPatientProfileResult
    }

    override suspend fun updatePatientProfile(
        patientId: String,
        name: String,
        phone: String?,
        dateOfBirth: String?,
    ): Result<PatientProfile> {
        updatePatientProfileCallCount++
        return updatePatientProfileResult
    }

    override suspend fun getMedicalProfile(patientId: String): Result<MedicalProfile> {
        getMedicalProfileCallCount++
        return getMedicalProfileResult
    }

    override suspend fun updateMedicalProfile(patientId: String, profile: MedicalProfile): Result<MedicalProfile> {
        updateMedicalProfileCallCount++
        return updateMedicalProfileResult
    }

    override suspend fun getDashboard(patientId: String): Result<PatientDashboard> {
        getDashboardCallCount++
        return getDashboardResult
    }

    override suspend fun deleteAccount(password: String, reason: String?): Result<Unit> {
        deleteAccountCallCount++
        return deleteAccountResult
    }

    override suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> {
        changePasswordCallCount++
        lastChangePasswordCurrent = currentPassword
        lastChangePasswordNew = newPassword
        return changePasswordResult
    }
}
