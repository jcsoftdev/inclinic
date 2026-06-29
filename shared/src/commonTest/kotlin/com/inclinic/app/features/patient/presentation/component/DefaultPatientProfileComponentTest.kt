@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.inclinic.app.features.patient.presentation.component

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import com.inclinic.app.core.model.MedicalProfile
import com.inclinic.app.core.model.PatientProfile
import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.patient.infrastructure.remote.PatientDashboard
import com.inclinic.app.features.patient.infrastructure.remote.PatientDataSource
import com.inclinic.app.features.patient.profile.application.GetMedicalProfileUseCase
import com.inclinic.app.features.patient.profile.application.GetPatientProfileUseCase
import com.inclinic.app.features.patient.profile.application.UpdatePatientProfileUseCase
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

private fun testProfile() = PatientProfile(
    id = "pat-1",
    name = "María López",
    email = "maria@test.com",
    phone = "999888777",
    dateOfBirth = "1990-05-15",
    photoUrl = null,
)

private class FakeProfilePatientDataSource(
    private val profileResult: Result<PatientProfile> = Result.success(testProfile()),
    private val updateResult: Result<PatientProfile> = Result.success(testProfile()),
) : PatientDataSource {
    var updateCallCount = 0
    var lastUpdatedName: String? = null

    override suspend fun getPatientProfile(patientId: String): Result<PatientProfile> = profileResult

    override suspend fun updatePatientProfile(
        patientId: String,
        name: String,
        phone: String?,
        dateOfBirth: String?,
    ): Result<PatientProfile> {
        updateCallCount++
        lastUpdatedName = name
        return updateResult
    }

    override suspend fun getMedicalProfile(patientId: String): Result<MedicalProfile> =
        Result.success(MedicalProfile.empty())

    override suspend fun updateMedicalProfile(patientId: String, profile: MedicalProfile): Result<MedicalProfile> =
        Result.success(profile)

    override suspend fun getDashboard(patientId: String): Result<PatientDashboard> =
        Result.success(PatientDashboard(upcomingCount = 0, recentDoctors = emptyList()))

    override suspend fun deleteAccount(password: String, reason: String?): Result<Unit> =
        Result.success(Unit)
}

class DefaultPatientProfileComponentTest {

    private val dispatchers = TestAppDispatchers()
    private val lifecycle = LifecycleRegistry().also { it.resume() }
    private val ctx = DefaultComponentContext(lifecycle = lifecycle)

    private fun createComponent(
        dataSource: FakeProfilePatientDataSource = FakeProfilePatientDataSource(),
        outputs: MutableList<PatientProfileComponent.Output> = mutableListOf(),
    ): DefaultPatientProfileComponent = DefaultPatientProfileComponent(
        componentContext = ctx,
        patientId = "pat-1",
        getProfile = GetPatientProfileUseCase(dataSource, dispatchers),
        getMedicalProfile = GetMedicalProfileUseCase(dataSource, dispatchers),
        updateProfile = UpdatePatientProfileUseCase(dataSource, dispatchers),
        dispatchers = dispatchers,
        onOutput = outputs::add,
    )

    @Test
    fun load_success_populates_state_with_profile_fields() = runTest {
        val component = createComponent()

        val state = component.state.value
        assertFalse(state.isLoading)
        assertNotNull(state.profile)
        assertEquals("pat-1", state.profile!!.id)
        assertEquals("María López", state.name)
        assertEquals("999888777", state.phone)
        assertEquals("1990-05-15", state.dateOfBirth)
        assertNull(state.error)
    }

    @Test
    fun load_failure_sets_error_in_state() = runTest {
        val ds = FakeProfilePatientDataSource(profileResult = Result.failure(Exception("Not found")))
        val component = createComponent(dataSource = ds)

        val state = component.state.value
        assertFalse(state.isLoading)
        assertNull(state.profile)
        assertEquals("Not found", state.error)
    }

    @Test
    fun onNameChange_updates_name_in_state() = runTest {
        val component = createComponent()

        component.onNameChange("Carlos Ríos")

        assertEquals("Carlos Ríos", component.state.value.name)
    }

    @Test
    fun onPhoneChange_updates_phone_in_state() = runTest {
        val component = createComponent()

        component.onPhoneChange("987654321")

        assertEquals("987654321", component.state.value.phone)
    }

    @Test
    fun onDateOfBirthChange_updates_dob_in_state() = runTest {
        val component = createComponent()

        component.onDateOfBirthChange("1985-03-20")

        assertEquals("1985-03-20", component.state.value.dateOfBirth)
    }

    @Test
    fun onSave_with_changed_name_calls_update_and_clears_isSaving() = runTest {
        val ds = FakeProfilePatientDataSource()
        val component = createComponent(dataSource = ds)
        component.onNameChange("Nuevo Nombre")

        component.onSave()

        assertEquals(1, ds.updateCallCount)
        assertEquals("Nuevo Nombre", ds.lastUpdatedName)
        assertFalse(component.state.value.isSaving)
    }

    @Test
    fun onSave_with_no_changes_does_not_call_update() = runTest {
        val ds = FakeProfilePatientDataSource()
        val component = createComponent(dataSource = ds)

        component.onSave()

        assertEquals(0, ds.updateCallCount)
    }

    @Test
    fun onSave_failure_sets_error_and_clears_isSaving() = runTest {
        val ds = FakeProfilePatientDataSource(
            updateResult = Result.failure(Exception("Save failed")),
        )
        val component = createComponent(dataSource = ds)
        component.onNameChange("Otro Nombre")

        component.onSave()

        assertFalse(component.state.value.isSaving)
        assertEquals("Save failed", component.state.value.error)
    }

    @Test
    fun onBack_emits_Back_output() = runTest {
        val outputs = mutableListOf<PatientProfileComponent.Output>()
        val component = createComponent(outputs = outputs)

        component.onBack()

        assertEquals(1, outputs.size)
        assertTrue(outputs.first() is PatientProfileComponent.Output.Back)
    }

    @Test
    fun onErrorDismissed_clears_error() = runTest {
        val ds = FakeProfilePatientDataSource(profileResult = Result.failure(Exception("Error")))
        val component = createComponent(dataSource = ds)
        assertNotNull(component.state.value.error)

        component.onErrorDismissed()

        assertNull(component.state.value.error)
    }

    @Test
    fun onToggleEdit_enables_then_disables_edit_mode() = runTest {
        val component = createComponent()
        assertFalse(component.state.value.isEditing)

        component.onToggleEdit()
        assertTrue(component.state.value.isEditing)

        component.onToggleEdit()
        assertFalse(component.state.value.isEditing)
    }

    @Test
    fun onToggleEdit_off_discards_unsaved_edits() = runTest {
        val component = createComponent()
        component.onToggleEdit()
        component.onNameChange("Edited Name")

        component.onToggleEdit()

        assertEquals("María López", component.state.value.name)
    }

    @Test
    fun onSave_success_exits_edit_mode_and_emits_Saved() = runTest {
        val outputs = mutableListOf<PatientProfileComponent.Output>()
        val component = createComponent(outputs = outputs)
        component.onToggleEdit()
        component.onNameChange("Nuevo Nombre")

        component.onSave()

        assertFalse(component.state.value.isEditing)
        assertTrue(outputs.any { it is PatientProfileComponent.Output.Saved })
    }

    @Test
    fun onBack_emits_Back_output() = runTest {
        val outputs = mutableListOf<PatientProfileComponent.Output>()
        val component = createComponent(outputs = outputs)

        component.onBack()

        assertEquals(1, outputs.size)
        assertTrue(outputs.first() is PatientProfileComponent.Output.Back)
    }
}
