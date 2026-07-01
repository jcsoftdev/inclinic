@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.inclinic.app.features.patient.presentation.component

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import com.inclinic.app.core.model.EmergencyContact
import com.inclinic.app.core.model.MedicalProfile
import com.inclinic.app.core.model.PatientProfile
import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.patient.infrastructure.remote.PatientDashboard
import com.inclinic.app.features.patient.infrastructure.remote.PatientDataSource
import com.inclinic.app.features.patient.profile.application.GetMedicalProfileUseCase
import com.inclinic.app.features.patient.profile.application.UpdateClinicalProfileUseCase
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

// ── Fake data source ──────────────────────────────────────────────────────────

private class FakeClinicalPatientDataSource(
    private val getResult: Result<MedicalProfile> = Result.success(MedicalProfile.empty()),
    private val updateResult: Result<MedicalProfile> = Result.success(MedicalProfile.empty()),
) : PatientDataSource {
    var updateCallCount = 0
    var lastUpdatePatientId: String? = null
    var lastUpdateProfile: MedicalProfile? = null

    override suspend fun getPatientProfile(patientId: String): Result<PatientProfile> =
        Result.failure(UnsupportedOperationException())

    override suspend fun updatePatientProfile(patientId: String, name: String, phone: String?, dateOfBirth: String?): Result<PatientProfile> =
        Result.failure(UnsupportedOperationException())

    override suspend fun getMedicalProfile(patientId: String): Result<MedicalProfile> = getResult

    override suspend fun updateMedicalProfile(patientId: String, profile: MedicalProfile): Result<MedicalProfile> {
        updateCallCount++
        lastUpdatePatientId = patientId
        lastUpdateProfile = profile
        return updateResult
    }

    override suspend fun getDashboard(patientId: String): Result<PatientDashboard> =
        Result.failure(UnsupportedOperationException())

    override suspend fun deleteAccount(password: String, reason: String?): Result<Unit> =
        Result.failure(UnsupportedOperationException())

    override suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> =
        Result.failure(UnsupportedOperationException())
}

// ── Component factory ─────────────────────────────────────────────────────────

class DefaultClinicalProfileComponentTest {

    private val dispatchers = TestAppDispatchers()
    private val lifecycle = LifecycleRegistry().also { it.resume() }
    private val ctx = DefaultComponentContext(lifecycle = lifecycle)

    private fun createComponent(
        dataSource: FakeClinicalPatientDataSource = FakeClinicalPatientDataSource(),
        outputs: MutableList<ClinicalProfileComponent.Output> = mutableListOf(),
    ): DefaultClinicalProfileComponent = DefaultClinicalProfileComponent(
        componentContext = ctx,
        patientId = "pat-1",
        getProfile = GetMedicalProfileUseCase(dataSource, dispatchers),
        updateProfile = UpdateClinicalProfileUseCase(dataSource, dispatchers),
        dispatchers = dispatchers,
        onOutput = outputs::add,
    )

    // ── Load ─────────────────────────────────────────────────────────────────

    @Test
    fun loads_profile_on_init_success() = runTest {
        val profile = MedicalProfile(
            bloodType = "O+",
            allergies = listOf("Penicilina"),
            chronicConditions = listOf("Diabetes"),
            heightCm = 175f,
            emergencyContact = EmergencyContact(name = "Mamá", phone = "+51999", relation = "madre"),
        )
        val ds = FakeClinicalPatientDataSource(getResult = Result.success(profile))
        val component = createComponent(dataSource = ds)

        val s = component.state.value
        assertFalse(s.isLoading)
        assertEquals("O+", s.bloodType)
        assertEquals(listOf("Penicilina"), s.allergies)
        assertEquals(listOf("Diabetes"), s.conditions)
        assertEquals(175f, s.heightCm)
        assertEquals("Mamá", s.emergencyContactName)
        assertNull(s.error)
    }

    @Test
    fun loads_profile_on_init_failure_sets_error() = runTest {
        val ds = FakeClinicalPatientDataSource(getResult = Result.failure(Exception("Network error")))
        val component = createComponent(dataSource = ds)

        val s = component.state.value
        assertFalse(s.isLoading)
        assertNotNull(s.error)
        assertTrue(s.error!!.contains("Network error") || s.error!!.contains("cargar"))
    }

    // ── Edit toggle ───────────────────────────────────────────────────────────

    @Test
    fun toggle_edit_populates_drafts_from_loaded_state() = runTest {
        val profile = MedicalProfile(
            bloodType = "AB-",
            allergies = listOf("Mariscos"),
            heightCm = 168f,
        )
        val ds = FakeClinicalPatientDataSource(getResult = Result.success(profile))
        val component = createComponent(dataSource = ds)

        component.onToggleEdit()

        val s = component.state.value
        assertTrue(s.isEditing)
        assertEquals("AB-", s.draftBloodType)
        assertEquals("168", s.draftHeightCm)
        assertEquals("Mariscos", s.draftAllergies)
    }

    @Test
    fun cancel_edit_reverts_drafts() = runTest {
        val profile = MedicalProfile(bloodType = "A+")
        val ds = FakeClinicalPatientDataSource(getResult = Result.success(profile))
        val component = createComponent(dataSource = ds)

        component.onToggleEdit()
        component.onBloodTypeChange("X!")    // invalid change
        component.onToggleEdit()             // cancel

        val s = component.state.value
        assertFalse(s.isEditing)
        // State.bloodType should remain from original load
        assertEquals("A+", s.bloodType)
    }

    // ── Save ─────────────────────────────────────────────────────────────────

    @Test
    fun save_calls_datasource_with_parsed_fields() = runTest {
        val saved = MedicalProfile(bloodType = "B+", allergies = listOf("Nueces", "Leche"), heightCm = 170f)
        val ds = FakeClinicalPatientDataSource(updateResult = Result.success(saved))
        val component = createComponent(dataSource = ds)

        component.onToggleEdit()
        component.onBloodTypeChange("B+")
        component.onHeightCmChange("170")
        component.onAllergiesChange("Nueces, Leche")
        component.onSave()

        assertEquals(1, ds.updateCallCount)
        assertEquals("pat-1", ds.lastUpdatePatientId)
        assertEquals("B+", ds.lastUpdateProfile?.bloodType)
        // Comma-split: "Nueces", "Leche"
        assertEquals(2, ds.lastUpdateProfile?.allergies?.size)
    }

    @Test
    fun save_success_exits_edit_mode_and_applies_loaded_state() = runTest {
        val saved = MedicalProfile(bloodType = "B-")
        val ds = FakeClinicalPatientDataSource(updateResult = Result.success(saved))
        val component = createComponent(dataSource = ds)

        component.onToggleEdit()
        component.onBloodTypeChange("B-")
        component.onSave()

        val s = component.state.value
        assertFalse(s.isEditing)
        assertFalse(s.isSaving)
        assertEquals("B-", s.bloodType)
        assertNull(s.error)
    }

    @Test
    fun save_failure_sets_error_and_stays_in_edit_mode() = runTest {
        val ds = FakeClinicalPatientDataSource(updateResult = Result.failure(Exception("Save failed")))
        val component = createComponent(dataSource = ds)

        component.onToggleEdit()
        component.onSave()

        val s = component.state.value
        assertTrue(s.isEditing)
        assertFalse(s.isSaving)
        assertNotNull(s.error)
    }

    // ── Navigation ────────────────────────────────────────────────────────────

    @Test
    fun onBack_emits_Back_output() {
        val outputs = mutableListOf<ClinicalProfileComponent.Output>()
        val component = createComponent(outputs = outputs)

        component.onBack()

        assertEquals(1, outputs.size)
        assertEquals(ClinicalProfileComponent.Output.Back, outputs.first())
    }

    // ── Error dismiss ─────────────────────────────────────────────────────────

    @Test
    fun onDismissError_clears_error() = runTest {
        val ds = FakeClinicalPatientDataSource(getResult = Result.failure(Exception("oops")))
        val component = createComponent(dataSource = ds)

        assertNotNull(component.state.value.error)
        component.onDismissError()
        assertNull(component.state.value.error)
    }
}
