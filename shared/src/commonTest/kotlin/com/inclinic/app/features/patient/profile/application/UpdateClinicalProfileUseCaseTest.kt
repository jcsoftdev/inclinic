@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.inclinic.app.features.patient.profile.application

import com.inclinic.app.core.model.Appointment
import com.inclinic.app.core.model.EmergencyContact
import com.inclinic.app.core.model.MedicalProfile
import com.inclinic.app.core.model.PatientProfile
import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.patient.infrastructure.remote.PatientDataSource
import com.inclinic.app.features.patient.infrastructure.remote.PatientDashboard
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

// ── Fake ─────────────────────────────────────────────────────────────────────

private class FakePatientDataSource : PatientDataSource {
    var updateResult: Result<MedicalProfile> = Result.success(MedicalProfile.empty())
    var lastUpdateProfile: MedicalProfile? = null

    override suspend fun getPatientProfile(patientId: String): Result<PatientProfile> =
        Result.failure(UnsupportedOperationException())

    override suspend fun updatePatientProfile(patientId: String, name: String, phone: String?, dateOfBirth: String?): Result<PatientProfile> =
        Result.failure(UnsupportedOperationException())

    override suspend fun getMedicalProfile(patientId: String): Result<MedicalProfile> =
        Result.success(MedicalProfile.empty())

    override suspend fun updateMedicalProfile(patientId: String, profile: MedicalProfile): Result<MedicalProfile> {
        lastUpdateProfile = profile
        return updateResult
    }

    override suspend fun getDashboard(patientId: String): Result<PatientDashboard> =
        Result.failure(UnsupportedOperationException())

    override suspend fun deleteAccount(password: String, reason: String?): Result<Unit> =
        Result.failure(UnsupportedOperationException())
}

// ── Tests ─────────────────────────────────────────────────────────────────────

class UpdateClinicalProfileUseCaseTest {

    private val fake = FakePatientDataSource()
    private val useCase = UpdateClinicalProfileUseCase(dataSource = fake, dispatchers = TestAppDispatchers())

    @Test
    fun success_returns_updated_profile() = runTest {
        val expected = MedicalProfile(
            bloodType = "O+",
            allergies = listOf("Penicilina"),
            chronicConditions = listOf("Diabetes"),
            heightCm = 175f,
            weightKg = 70f,
            emergencyContact = EmergencyContact(name = "Mamá", phone = "+51999111222", relation = "madre"),
        )
        fake.updateResult = Result.success(expected)

        val result = useCase("patient-1", expected)

        assertTrue(result.isSuccess)
        assertEquals("O+", result.getOrNull()?.bloodType)
        assertEquals(listOf("Penicilina"), result.getOrNull()?.allergies)
        // Verify the profile was passed through to data source
        assertEquals("O+", fake.lastUpdateProfile?.bloodType)
    }

    @Test
    fun failure_propagates_error() = runTest {
        fake.updateResult = Result.failure(Exception("Network error"))

        val result = useCase("patient-1", MedicalProfile.empty())

        assertTrue(result.isFailure)
        assertEquals("Network error", result.exceptionOrNull()?.message)
    }

    @Test
    fun passes_all_fields_to_datasource() = runTest {
        val profile = MedicalProfile(
            bloodType = "AB-",
            allergies = listOf("Mariscos", "Nueces"),
            chronicConditions = listOf("Hipertensión"),
            heightCm = 168f,
            weightKg = 62f,
            emergencyContact = EmergencyContact(name = "Juan", phone = "+51987654321", relation = "padre"),
        )
        fake.updateResult = Result.success(profile)

        useCase("patient-99", profile)

        val passed = fake.lastUpdateProfile!!
        assertEquals("AB-", passed.bloodType)
        assertEquals(2, passed.allergies.size)
        assertEquals("Hipertensión", passed.chronicConditions.first())
        assertEquals("Juan", passed.emergencyContact.name)
    }
}
